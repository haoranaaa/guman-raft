package com.guman.raft.impl;

import com.google.common.base.Splitter;
import com.guman.raft.*;
import com.guman.raft.common.NodeConfig;
import com.guman.raft.common.current.RaftThreadPool;
import com.guman.raft.common.util.LongConvert;
import com.guman.raft.constant.ServerState;
import com.guman.raft.model.*;
import com.guman.raft.model.client.ClientKVAck;
import com.guman.raft.model.client.ClientPairReq;
import com.guman.raft.rpc.DefaultRpcServer;
import com.guman.raft.rpc.RpcClient;
import com.guman.raft.rpc.RpcServer;
import com.guman.raft.rpc.model.Request;
import com.guman.raft.rpc.model.Response;
import com.guman.raft.store.DefaultLogModule;
import com.guman.raft.store.DefaultStore;
import com.guman.raft.thread.DefaultThreadPool;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.guman.raft.constant.ServerState.LEADER;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author duanhaoran
 * @since 2020/3/28 7:09 PM
 */
@Slf4j
@Data
public class DefaultNode<T> implements Node<T>, LifeCycle {

    private AtomicBoolean start = new AtomicBoolean(Boolean.FALSE);

    private static Splitter splitter = Splitter.on(":");

    /**
     * 选举时间间隔基数
     */
    public volatile long electionTime = 15 * 1000;

    /**
     * 上一次选举时间
     */
    public volatile long preElectionTime = 0;

    /**
     * 上次一心跳时间戳
     */
    public volatile long preHeartBeatTime = 0;

    /**
     * 心跳间隔基数
     */
    public final long heartBeatTick = 5 * 1000;

    private HeartBeatJob heartBeatJob = new HeartBeatJob();

    private ElectionJob electionJob = new ElectionJob();

    private ReplicationFailQueueConsumer replicationFailQueueConsumer = new ReplicationFailQueueConsumer();

    private LinkedBlockingQueue<ReplicationFailModel> replicationFailQueue = new LinkedBlockingQueue<>(2048);


    /* -------------- 所有服务器上持久存在的 ----------- */

    /**
     * 服务器最后一次知道的任期号（初始化为 0，持续递增）
     */
    volatile long currentTerm = 0;

    /**
     * 在当前获得选票的候选人的 Id
     */
    volatile int votedFor = -1;

    /**
     * 日志条目集；每一个条目包含一个用户状态机执行的指令，和收到时的任期号
     */
    LogModule logModule;

    public Store stateMachine;


    /* --------------- 所有服务器上经常变的 ------------- */

    /**
     * 已知的最大的已经被提交的日志条目的索引值
     */
    volatile long commitIndex;

    /**
     * 最后被应用到状态机的日志条目索引值（初始化为 0，持续递增)
     */
    volatile long lastApplied = 0;

    /* --------------- 在领导人里经常改变的(选举后重新初始化) --------------- */

    /**
     * 对于每一个服务器，需要发送给他的下一个日志条目的索引值（初始化为领导人最后索引值加一）
     */
    Map<Peer, Long> nextIndexs;

    /**
     * 对于每一个服务器，已经复制给他的日志的最高索引值
     */
    Map<Peer, Long> matchIndexs;
    /**
     * 一致性实现
     */
    Consensus consensus;

    private NodeConfig nodeConfig;

    private RpcServer rpcServer;

    private RpcClient rpcClient;

    private Store store;

    private PeerSet peerSet;

    /**
     * 当前服务的状态
     */
    private ServerState serverState = ServerState.FOLLOWER;


    private static class DefaultNodeLazyHolder {
        private static final DefaultNode INSTANCE = new DefaultNode();
    }

    public static DefaultNode getInstance() {
        return DefaultNodeLazyHolder.INSTANCE;
    }

    @Override
    public void init() {
        if (!start.compareAndSet(Boolean.FALSE, Boolean.TRUE)) {
            return;
        }
        synchronized (this) {
            rpcServer.init();

            DefaultThreadPool.scheduleWithFixedDelay(heartBeatJob, 500);
            DefaultThreadPool.scheduleAtFixedRate(electionJob, 5000, 500);
        }
    }


    @Override
    public void setConfig(NodeConfig config) {
        if (config == null) {
            config = new NodeConfig();
        }
        this.nodeConfig = config;
        this.store = DefaultStore.getInstance();
        this.logModule = DefaultLogModule.getInstance();
        List<String> otherAddresses = config.getOtherAddresses();
        if (CollectionUtils.isEmpty(otherAddresses)) {
            throw new RuntimeException("address can not be null ！");
        }
        peerSet = PeerSet.getInstance();
        try {
            NodeConfig finalConfig = config;
            List<Peer> peers = otherAddresses.stream().map(i -> splitter.splitToList(i))
                    .map(i -> new Peer(Integer.valueOf(i.get(0)), i.get(1), Integer.valueOf(i.get(2))))
                    .peek(i -> {
                        if (Objects.equals(i.getNodeId(), finalConfig.getNodeId())
                                && Objects.equals(i.getPort(), i.getPort())) {
                            peerSet.setSelf(i);
                        }
                    }).collect(Collectors.toList());
            peerSet.setList(peers);
        } catch (Exception e) {
            log.error("格式化节点地址失败！ ", e);
            throw e;
        }
        rpcServer = new DefaultRpcServer(config.getSelfPort(), this);
    }

    @Override
    public VoteResult handleRequestVote(RemoteParam param) {
        return consensus.requestForVote(param);
    }

    @Override
    public EntryResult handleRequestAppendLog(BaseEntryParams entryParam) {
        return consensus.appendLogEntries(entryParam);
    }

    /**
     * 客户端的每一个请求都包含一条被复制状态机执行的指令。
     * 领导人把这条指令作为一条新的日志条目附加到日志中去，然后并行的发起附加条目 RPCs 给其他的服务器，让他们复制这条日志条目。
     * 当这条日志条目被安全的复制（下面会介绍），领导人会应用这条日志条目到它的状态机中然后把执行的结果返回给客户端。
     * 如果跟随者崩溃或者运行缓慢，再或者网络丢包，
     * 领导人会不断的重复尝试附加日志条目 RPCs （尽管已经回复了客户端）直到所有的跟随者都最终存储了所有的日志条目。
     *
     * @param request
     * @return
     */
    @Override
    public ClientKVAck handleClientRequest(ClientPairReq request) {
        log.warn("handlerClientRequest handler {} operation,  and key : [{}], value : [{}]",
                ClientPairReq.Type.value(request.getType()), request.getKey(), request.getValue());
        if (serverState != LEADER) {
            log.warn("I not am leader , only invoke redirect method, leader addr : {}, my nodeId : {}",
                    peerSet.getLeader(), peerSet.getSelf().getNodeId());
            return redirect(request);
        }

        if (request.getType() == ClientPairReq.GET) {
            EntryParam logEntry = stateMachine.get(request.getKey());
            if (logEntry != null) {
                return new ClientKVAck(logEntry.getPair());
            }
            return new ClientKVAck(null);
        }

        EntryParam logEntry = EntryParam.builder()
                .pair(Pair.builder().
                        key(request.getKey()).
                        value(request.getValue()).
                        build())
                .term(currentTerm)
                .build();

        // 预提交到本地日志, todo 预提交
        logModule.write(logEntry);
        log.info("write logModule success, logEntry info : {}, log index : {}", logEntry, logEntry.getIndex());

        final AtomicInteger success = new AtomicInteger(0);

        List<Future<Boolean>> futureList = new CopyOnWriteArrayList<>();

        int count = 0;
        //  复制到其他机器
        for (Peer peer : peerSet.getPeersWithOutSelf()) {
            count++;
            // 并行发起 RPC 复制.
            futureList.add(replication(peer, logEntry));
        }

        CountDownLatch latch = new CountDownLatch(futureList.size());
        List<Boolean> resultList = new CopyOnWriteArrayList<>();

        getRPCAppendResult(futureList, latch, resultList);

        try {
            latch.await(4000, MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Boolean aBoolean : resultList) {
            if (aBoolean) {
                success.incrementAndGet();
            }
        }

        // 如果存在一个满足N > commitIndex的 N，并且大多数的matchIndex[i] ≥ N成立，
        // 并且log[N].term == currentTerm成立，那么令 commitIndex 等于这个 N （5.3 和 5.4 节）
        List<Long> matchIndexList = new ArrayList<>(matchIndexs.values());
        // 小于 2, 没有意义
        int median = 0;
        if (matchIndexList.size() >= 2) {
            Collections.sort(matchIndexList);
            median = matchIndexList.size() / 2;
        }
        Long N = matchIndexList.get(median);
        if (N > commitIndex) {
            EntryParam entry = logModule.read(N);
            if (entry != null && entry.getTerm() == currentTerm) {
                commitIndex = N;
            }
        }

        //  响应客户端(成功一半)
        if (success.get() >= (count / 2)) {
            // 更新
            commitIndex = logEntry.getIndex();
            //  应用到状态机
            getStateMachine().apply(logEntry);
            lastApplied = commitIndex;

            log.info("success apply local state machine,  logEntry info : {}", logEntry);
            // 返回成功.
            return ClientKVAck.ok();
        } else {
            // 回滚已经提交的日志.
            logModule.removeOnStartIndex(logEntry.getIndex());
            log.warn("fail apply local state  machine,  logEntry info : {}", logEntry);
            // 这里应该返回错误, 因为没有成功复制过半机器.
            return ClientKVAck.fail();
        }
    }

    private void getRPCAppendResult(List<Future<Boolean>> futureList, CountDownLatch latch, List<Boolean> resultList) {
        for (Future<Boolean> future : futureList) {
            RaftThreadPool.execute(() -> {
                try {
                    resultList.add(future.get(3000, MILLISECONDS));
                } catch (CancellationException | TimeoutException | ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                    resultList.add(false);
                } finally {
                    latch.countDown();
                }
            });
        }

    }

    private Future<Boolean> replication(Peer peer, EntryParam entry) {
        return RaftThreadPool.submit(new Callable() {
            @Override
            public Boolean call() throws Exception {

                long start = System.currentTimeMillis(), end = start;

                // 20 秒重试时间
                while (end - start < 20 * 1000L) {

                    BaseEntryParams baseEntryParams = new BaseEntryParams();
                    baseEntryParams.setTerm(currentTerm);
                    baseEntryParams.setServiceId(peer.getNodeId());
                    baseEntryParams.setLeaderId(peerSet.getSelf().getNodeId());

                    baseEntryParams.setLeaderCommit(commitIndex);

                    // 以我这边为准, 这个行为通常是成为 leader 后,首次进行 RPC 才有意义.
                    Long nextIndex = nextIndexs.get(peer);
                    LinkedList<EntryParam> logEntries = new LinkedList<>();
                    if (entry.getIndex() >= nextIndex) {
                        for (long i = nextIndex; i <= entry.getIndex(); i++) {
                            EntryParam l = logModule.read(i);
                            if (l != null) {
                                logEntries.add(l);
                            }
                        }
                    } else {
                        logEntries.add(entry);
                    }
                    // 最小的那个日志.
                    EntryParam preLog = getPreLog(logEntries.getFirst());
                    baseEntryParams.setPreLogTerm(preLog.getTerm());
                    baseEntryParams.setPrevLogIndex(preLog.getIndex());

                    baseEntryParams.setEntryParams(logEntries);

                    Request request = Request.builder()
                            .cmd(Request.A_ENTRIES)
                            .obj(baseEntryParams)
                            .nodeId(peer.getNodeId())
                            .build();

                    try {
                        Response response = getRpcClient().send(request);
                        if (response == null) {
                            return false;
                        }
                        EntryResult result = (EntryResult) response.getResult();
                        if (result != null && result.isSuccess()) {
                            log.info("append follower entry success , follower=[{}], entry=[{}]", peer, baseEntryParams.getEntryParams());
                            // update 这两个追踪值
                            nextIndexs.put(peer, entry.getIndex() + 1);
                            matchIndexs.put(peer, entry.getIndex());
                            return true;
                        } else if (result != null) {
                            // 对方比我大
                            if (result.getTerm() > currentTerm) {
                                log.warn("follower [{}] term [{}] than more self, and my term = [{}], so, I will become follower",
                                        peer, result.getTerm(), currentTerm);
                                currentTerm = result.getTerm();
                                // 认怂, 变成跟随者
                                serverState = ServerState.FOLLOWER;
                                return false;
                            } // 没我大, 却失败了,说明 index 不对.或者 term 不对.
                            else {
                                // 递减
                                if (nextIndex == 0) {
                                    nextIndex = 1L;
                                }
                                nextIndexs.put(peer, nextIndex - 1);
                                log.warn("follower {} nextIndex not match, will reduce nextIndex and retry RPC append, nextIndex : [{}]", peer.getNodeId(),
                                        nextIndex);
                                // 重来, 直到成功.
                            }
                        }

                        end = System.currentTimeMillis();

                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                        //放队列重试
                        ReplicationFailModel model = ReplicationFailModel.builder()
                                .callable(this)
                                .logEntry(entry)
                                .peer(peer)
                                .offerTime(System.currentTimeMillis())
                                .build();
                        replicationFailQueue.offer(model);
                        return false;
                    }
                }
                // 超时了,没办法了
                return false;
            }
        });
    }

    private EntryParam getPreLog(EntryParam logEntry) {
        EntryParam entry = logModule.read(logEntry.getIndex() - 1);

        if (entry == null) {
            log.warn("get perLog is null , parameter logEntry : {}", logEntry);
            entry = EntryParam.builder().index(0L).term(0).pair(null).build();
        }
        return entry;
    }

    private ClientKVAck redirect(ClientPairReq request) {
        Request<ClientPairReq> r = Request.<ClientPairReq>builder().
                obj(request).nodeId(peerSet.getLeader().getNodeId()).cmd(Request.CLIENT_REQ).build();
        Response response = rpcClient.send(r);
        return (ClientKVAck) response.getResult();
    }


    @Override
    public void destroy() {

    }

    /**
     * 客户端和服务端之间的心跳
     */
    class HeartBeatJob implements Runnable {

        @Override
        public void run() {
            if (serverState != LEADER) {
                return;
            }

            long current = System.currentTimeMillis();
            if (current - preHeartBeatTime < heartBeatTick) {
                return;
            }
            log.info("=========== NextIndex =============");
            for (Peer peer : peerSet.getPeersWithOutSelf()) {
                log.info("Peer {} nextIndex={}", peer.getNodeId(), nextIndexs.get(peer));
            }

            preHeartBeatTime = System.currentTimeMillis();

            // 心跳只关心 term 和 leaderID
            for (Peer peer : peerSet.getPeersWithOutSelf()) {

                BaseEntryParams param = BaseEntryParams.builder()
                        .entryParams(null)// 心跳,空日志.
                        .leaderId(peerSet.getSelf().getNodeId())
                        .build();
                param.setServiceId(peer.getNodeId());
                param.setTerm(currentTerm);
                Request<BaseEntryParams> request = new Request<>(
                        Request.A_ENTRIES,
                        peer.getNodeId(),
                        param
                );

                RaftThreadPool.execute(() -> {
                    try {
                        Response response = getRpcClient().send(request);
                        EntryResult entryResult = (EntryResult) response.getResult();
                        long term = entryResult.getTerm();

                        if (term > currentTerm) {
                            log.error("self will become follower, he's term : {}, my term : {}", term, currentTerm);
                            currentTerm = term;
                            votedFor = -1;
                            serverState = ServerState.FOLLOWER;
                        }
                    } catch (Exception e) {
                        log.error("HeartBeatTask RPC Fail, request URL : {} ", request.getNodeId());
                    }
                }, false);
            }
        }
    }

    /**
     * 1. 在转变成候选人后就立即开始选举过程
     * 自增当前的任期号（currentTerm）
     * 给自己投票
     * 重置选举超时计时器
     * 发送请求投票的 RPC 给其他所有服务器
     * 2. 如果接收到大多数服务器的选票，那么就变成领导人
     * 3. 如果接收到来自新的领导人的附加日志 RPC，转变成跟随者
     * 4. 如果选举过程超时，再次发起一轮选举
     */
    class ElectionJob implements Runnable {

        @Override
        public void run() {

            if (serverState == LEADER) {
                return;
            }

            long current = System.currentTimeMillis();
            // 基于 RAFT 的随机时间,解决冲突.
            electionTime = electionTime + ThreadLocalRandom.current().nextInt(50);
            if (current - preElectionTime < electionTime) {
                return;
            }
            serverState = ServerState.CANDIDATE;
            log.error("node {} will become CANDIDATE and start election leader, current term : [{}], LastEntry : [{}]",
                    peerSet.getSelf(), currentTerm, logModule.getLast());

            preElectionTime = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(200) + 150;

            currentTerm = currentTerm + 1;
            // 推荐自己.
            votedFor = peerSet.getSelf().getNodeId();

            List<Peer> peers = peerSet.getPeersWithOutSelf();

            ArrayList<Future> futureArrayList = new ArrayList<>();

            log.info("peerList size : {}, peer list content : {}", peers.size(), peers);

            // 发送请求
            for (Peer peer : peers) {

                futureArrayList.add(RaftThreadPool.submit(() -> {
                    long lastTerm = 0L;
                    EntryParam last = logModule.getLast();
                    if (last != null) {
                        lastTerm = last.getTerm();
                    }

                    RemoteParam param = RemoteParam.builder().
                            candidateId(peerSet.getSelf().getNodeId()).
                            lastLogIndex(LongConvert.convert(logModule.getLastIndex())).
                            lastLogTerm(lastTerm).
                            build();
                    param.setTerm(currentTerm);

                    Request request = Request.builder()
                            .cmd(Request.R_VOTE)
                            .obj(param)
                            .nodeId(peer.getNodeId())
                            .build();

                    try {
                        @SuppressWarnings("unchecked")
                        Response<VoteResult> response = getRpcClient().send(request);
                        return response;

                    } catch (Exception e) {
                        log.error("ElectionTask RPC Fail , URL : " + request.getNodeId());
                        return null;
                    }
                }));
            }

            AtomicInteger success2 = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(futureArrayList.size());

            log.info("futureArrayList.size() : {}", futureArrayList.size());
            // 等待结果.
            for (Future future : futureArrayList) {
                RaftThreadPool.submit(() -> {
                    try {

                        @SuppressWarnings("unchecked")
                        Response<VoteResult> response = (Response<VoteResult>) future.get(3000, MILLISECONDS);
                        if (response == null) {
                            return -1;
                        }
                        boolean isVoteGranted = response.getResult().isVoteGranted();

                        if (isVoteGranted) {
                            success2.incrementAndGet();
                        } else {
                            // 更新自己的任期.
                            long resTerm = response.getResult().getTerm();
                            if (resTerm >= currentTerm) {
                                currentTerm = resTerm;
                            }
                        }
                        return 0;
                    } catch (Exception e) {
                        log.error("future.get exception , e : ", e);
                        return -1;
                    } finally {
                        latch.countDown();
                    }
                });
            }

            try {
                // 稍等片刻
                latch.await(3500, MILLISECONDS);
            } catch (InterruptedException e) {
                log.warn("InterruptedException By Master election Task");
            }

            int success = success2.get();
            log.info("node {} maybe become leader , success count = {} , status : {}", peerSet.getSelf(), success, serverState);
            // 如果投票期间,有其他服务器发送 appendEntry , 就可能变成 follower ,这时,应该停止.
            if (serverState == ServerState.FOLLOWER) {
                return;
            }
            // 加上自身.
            if (success >= peers.size() / 2) {
                log.warn("node {} become leader ", peerSet.getSelf());
                serverState = LEADER;
                peerSet.setLeader(peerSet.getSelf());
                votedFor = -1;
                becomeLeaderToDoThing();
            } else {
                // else 重新选举
                votedFor = -1;
            }
            
        }
    }
    /**
     * 初始化所有的 nextIndex 值为自己的最后一条日志的 index + 1. 如果下次 RPC 时, 跟随者和leader 不一致,就会失败.
     * 那么 leader 尝试递减 nextIndex 并进行重试.最终将达成一致.
     */
    private void becomeLeaderToDoThing() {
        nextIndexs = new ConcurrentHashMap<>();
        matchIndexs = new ConcurrentHashMap<>();
        for (Peer peer : peerSet.getPeersWithOutSelf()) {
            nextIndexs.put(peer, logModule.getLastIndex() + 1);
            matchIndexs.put(peer, 0L);
        }
    }


    class ReplicationFailQueueConsumer implements Runnable {

        /**
         * 一分钟
         */
        long intervalTime = 1000 * 60;

        @Override
        public void run() {
            for (; ; ) {

                try {
                    ReplicationFailModel model = replicationFailQueue.take();
                    if (serverState != LEADER) {
                        // 应该清空?
                        replicationFailQueue.clear();
                        continue;
                    }
                    log.warn("replication Fail Queue Consumer take a task, will be retry replication, content detail : [{}]", model.logEntry);
                    long offerTime = model.offerTime;
                    if (System.currentTimeMillis() - offerTime > intervalTime) {
                        log.warn("replication Fail event Queue maybe full or handler slow");
                    }

                    Callable callable = model.callable;
                    Future<Boolean> future = RaftThreadPool.submit(callable);
                    Boolean r = future.get(3000, MILLISECONDS);
                    // 重试成功.
                    if (r) {
                        // 可能有资格应用到状态机.
                        tryApplyStateMachine(model);
                    }

                } catch (InterruptedException e) {
                    // ignore
                } catch (ExecutionException | TimeoutException e) {
                    log.warn(e.getMessage());
                }
            }
        }
    }

    private void tryApplyStateMachine(ReplicationFailModel model) {

        String success = stateMachine.getString(model.successKey);
        stateMachine.setString(model.successKey, String.valueOf(Integer.valueOf(success) + 1));

        String count = stateMachine.getString(model.countKey);

        if (Integer.valueOf(success) >= Integer.valueOf(count) / 2) {
            stateMachine.apply(model.logEntry);
            stateMachine.delString(model.countKey, model.successKey);
        }
    }
}
