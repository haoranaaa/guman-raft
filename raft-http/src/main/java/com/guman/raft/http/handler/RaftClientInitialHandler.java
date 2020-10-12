package com.guman.raft.http.handler;

import com.guman.raft.http.codec.handler.server.RaftFrameDecoder;
import com.guman.raft.http.codec.handler.server.RaftFrameEncoder;
import com.guman.raft.http.codec.handler.client.RaftClientProtocolDecoder;
import com.guman.raft.http.codec.handler.client.RaftClientProtocolEncoder;
import com.guman.raft.http.codec.constant.NettyConstant;
import com.guman.raft.http.codec.dispatcher.RequestPendingCenter;
import com.guman.raft.http.codec.handler.client.ConnectionWatchdog;
import com.guman.raft.http.codec.handler.client.RequestPackageHandler;
import com.guman.raft.http.codec.handler.client.ResponseDispatchHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;

import java.util.concurrent.TimeUnit;

/**
 * @author duanhaoran
 * @since 2020/4/17 11:07 PM
 */
public class RaftClientInitialHandler extends ChannelInitializer<NioSocketChannel> {

    private int nodeId;

    private Bootstrap bootstrap;

    public RaftClientInitialHandler(Bootstrap bootstrap, RequestPendingCenter requestPendingCenter, int nodeId) {
        this.bootstrap = bootstrap;
        this.requestPendingCenter = requestPendingCenter;
        this.nodeId = nodeId;
    }

    private RequestPendingCenter requestPendingCenter;

    private LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);

    protected final HashedWheelTimer timer = new HashedWheelTimer();

    @Override
    protected void initChannel(NioSocketChannel channel) throws Exception {
        final ConnectionWatchdog watchdog = new ConnectionWatchdog(bootstrap, timer, nodeId);
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new RaftFrameDecoder());
        pipeline.addLast(new RaftFrameEncoder());
        pipeline.addLast(new RaftClientProtocolEncoder());
        pipeline.addLast(new RaftClientProtocolDecoder());
        pipeline.addLast(loggingHandler);
        pipeline.addLast(new ResponseDispatchHandler(requestPendingCenter));
        pipeline.addLast(
                new IdleStateHandler(NettyConstant.CLIENT_READ_IDEL_TIME_OUT, NettyConstant.CLIENT_WRITE_IDEL_TIME_OUT, NettyConstant.CLIENT_ALL_IDEL_TIME_OUT, TimeUnit.SECONDS));
        pipeline.addLast(new RequestPackageHandler());
        pipeline.addLast(watchdog);
    }
}
