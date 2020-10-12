package com.guman.raft.http;

import com.google.common.collect.Maps;
import com.guman.raft.common.util.IdGenUtil;
import com.guman.raft.common.util.NodeIdUtil;
import com.guman.raft.http.codec.constant.NettyConstant;
import com.guman.raft.http.codec.dispatcher.OperationResultFeature;
import com.guman.raft.http.codec.dispatcher.RequestPendingCenter;
import com.guman.raft.http.handler.RaftClientInitialHandler;
import com.guman.raft.http.msg.RequestMessage;
import com.guman.raft.http.msg.body.Operation;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.java.Log;

import java.util.Map;

/**
 * @author duanhaoran
 * @since 2020/4/12 7:49 PM
 */
@Log
public class RaftNettyClient {

    private static RequestPendingCenter requestPendingCenter = new RequestPendingCenter();

    public RaftNettyClient() {
        this.nodeId = NodeIdUtil.getNodeId();
        init();
    }

    /**
     * 要连接的nodeId
     */
    private int nodeId;

    private Bootstrap bootstrap;

    private Map<Integer, Channel> nodeChannel = Maps.newConcurrentMap();


    public void init() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new RaftClientInitialHandler(bootstrap, requestPendingCenter, nodeId));
    }

    public void connect(int nodeId) throws InterruptedException {
        NettyConstant.NodeLocal nodeLocal = NettyConstant.getNodeLocal(nodeId);
        ChannelFuture channelFuture = bootstrap.connect(nodeLocal.getHost(), nodeLocal.getNodeId());
        nodeChannel.put(nodeId, channelFuture.channel());
        channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
            if (channelFuture1.isSuccess()) {
                log.info("连接成功！");
            } else {
                connect(nodeId);
            }
        });
//        channelFuture.channel().closeFuture().sync();
    }

    public OperationResultFeature sendMsg(int nodeId, Operation operation) {
        Channel channel = nodeChannel.get(nodeId);
        if (channel == null) {
            try {
                connect(nodeId);
            } catch (InterruptedException e) {
                return OperationResultFeature.fail();
            }
        }
        RequestMessage requestMessage = new RequestMessage(IdGenUtil.gen(), operation);
        requestPendingCenter.add(requestMessage.getMessageHeader().getStreamId(), new OperationResultFeature());
        nodeChannel.get(nodeId).writeAndFlush(requestMessage);
        return requestPendingCenter.get(requestMessage.getMessageHeader().getStreamId());
    }


    public void destory() {
    }
}
