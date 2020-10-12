package com.guman.raft.impl;

import com.guman.raft.Consensus;
import com.guman.raft.constant.ServerState;
import com.guman.raft.model.*;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantLock;


/**
 *
 * 默认的一致性模块实现.
 *
 * @author duanhaoran
 */
@Data
public class DefaultConsensus implements Consensus {


    private static final Logger logger = LoggerFactory.getLogger(DefaultConsensus.class);


    public final DefaultNode node;

    public final ReentrantLock voteLock = new ReentrantLock();
    public final ReentrantLock appendLock = new ReentrantLock();

    public DefaultConsensus(DefaultNode node) {
        this.node = node;
    }


    @Override
    public VoteResult requestForVote(RemoteParam param) {
        try {
            VoteResult.VoteResultBuilder builder = VoteResult.builder();
            if (!voteLock.tryLock()) {
                return builder.term(node.getCurrentTerm()).voteGranted(false).build();
            }

            // 对方任期没有自己新
            if (param.getTerm() < node.getCurrentTerm()) {
                return builder.term(node.getCurrentTerm()).voteGranted(false).build();
            }

            // (当前节点并没有投票 或者 已经投票过了且是对方节点) && 对方日志和自己一样新
            logger.info("node {} current vote for [{}], param candidateId : {}", node.getPeerSet().getSelf(), node.getVotedFor(), param.getCandidateId());
            logger.info("node {} current term {}, peer term : {}", node.getPeerSet().getSelf(), node.getCurrentTerm(), param.getTerm());

            if (node.getVotedFor() < 0 || node.getVotedFor()== param.getCandidateId()) {

                if (node.getLogModule().getLast() != null) {
                    // 对方没有自己新
                    if (node.getLogModule().getLast().getTerm() > param.getLastLogTerm()) {
                        return VoteResult.fail();
                    }
                    // 对方没有自己新
                    if (node.getLogModule().getLastIndex() > param.getLastLogIndex()) {
                        return VoteResult.fail();
                    }
                }

                // 切换状态
                node.setServerState(ServerState.FOLLOWER);
                // 更新
                node.getPeerSet().setLeader(new Peer(param.getCandidateId()));
                node.setCurrentTerm(param.getTerm());
                node.setVotedFor(param.getServiceId());
                // 返回成功
                return builder.term(node.currentTerm).voteGranted(true).build();
            }

            return builder.term(node.currentTerm).voteGranted(false).build();

        } finally {
            voteLock.unlock();
        }
    }

    @Override
    public EntryResult appendLogEntries(BaseEntryParams param) {
        EntryResult result = EntryResult.fail();
        try {
            if (!appendLock.tryLock()) {
                return result;
            }

            result.setTerm(node.getCurrentTerm());
            // 不够格
            if (param.getTerm() < node.getCurrentTerm()) {
                return result;
            }

            node.preHeartBeatTime = System.currentTimeMillis();
            node.preElectionTime = System.currentTimeMillis();
            node.getPeerSet().setLeader(new Peer(param.getLeaderId()));

            // 够格
            if (param.getTerm() >= node.getCurrentTerm()) {
                logger.debug("node {} become FOLLOWER, currentTerm : {}, param Term : {}, param serverId",
                        node.getPeerSet().getSelf(), node.currentTerm, param.getTerm(), param.getServiceId());
                // 认怂
                node.setServerState(ServerState.FOLLOWER);
            }
            // 使用对方的 term.
            node.setCurrentTerm(param.getTerm());

            //心跳
            if (param.getEntryParams() == null || param.getEntryParams().size() == 0) {
                logger.info("node {} append heartbeat success , he's term : {}, my term : {}",
                        param.getLeaderId(), param.getTerm(), node.getCurrentTerm());
                return EntryResult.builder().term(node.getCurrentTerm()).success(true).build();
            }

            // 真实日志
            // 第一次
            if (node.getLogModule().getLastIndex() != 0 && param.getPrevLogIndex() != 0) {
                EntryParam logEntry;
                if ((logEntry = node.getLogModule().read(param.getPrevLogIndex())) != null) {
                    // 如果日志在 prevLogIndex 位置处的日志条目的任期号和 prevLogTerm 不匹配 则返回 false
                    // 需要减小 nextIndex 重试.
                    if (logEntry.getTerm() != param.getPreLogTerm()) {
                        return result;
                    }
                } else {
                    // index 不对, 需要递减 nextIndex 重试.
                    return result;
                }

            }

            // 如果已经存在的日志条目和新的产生冲突（索引值相同但是任期号不同） 删除这一条和之后所有的
            EntryParam existLog = node.getLogModule().read(((param.getPrevLogIndex() + 1)));
            if (existLog != null && existLog.getTerm() != param.getEntryParams().get(0).getTerm()) {
                // 删除这一条和之后所有的, 然后写入日志和状态机.
                node.getLogModule().removeOnStartIndex(param.getPrevLogIndex() + 1);
            } else if (existLog != null) {
                // 已经有日志了, 不能重复写入.
                result.setSuccess(true);
                return result;
            }

            // 写进日志并且应用到状态机
            for (EntryParam entry : param.getEntryParams()) {
                node.getLogModule().write(entry);
                node.stateMachine.apply(entry);
                result.setSuccess(true);
            }

            //如果 leaderCommit > commitIndex 令 commitIndex 等于 leaderCommit 和 新日志条目索引值中较小的一个
            if (param.getLeaderCommit() > node.getCommitIndex()) {
                int commitIndex = (int) Math.min(param.getLeaderCommit(), node.getLogModule().getLastIndex());
                node.setCommitIndex(commitIndex);
                node.setLastApplied(commitIndex);
            }

            result.setTerm(node.getCurrentTerm());

            node.setServerState(ServerState.FOLLOWER);
            // TODO, 是否应当在成功回复之后, 才正式提交? 防止 leader "等待回复"过程中 挂掉.
            return result;
        } finally {
            appendLock.unlock();
        }
    }
}
