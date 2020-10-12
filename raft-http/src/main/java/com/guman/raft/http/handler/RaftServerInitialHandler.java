package com.guman.raft.http.handler;

import com.guman.raft.http.codec.handler.server.RaftFrameDecoder;
import com.guman.raft.http.codec.handler.server.RaftFrameEncoder;
import com.guman.raft.http.codec.handler.server.ResponsePackageHandler;
import com.guman.raft.http.codec.server.RaftServerProtocolDecoder;
import com.guman.raft.http.codec.server.RaftServerProtocolEncoder;
import com.guman.raft.http.codec.handler.server.RaftServerProcessHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author duanhaoran
 * @since 2020/4/9 2:42 AM
 */
public class RaftServerInitialHandler extends ChannelInitializer<NioSocketChannel> {

    private LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);

    @Override
    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
        ChannelPipeline pipeline = nioSocketChannel.pipeline();
        pipeline.addLast(new RaftFrameDecoder());
        pipeline.addLast(new RaftFrameEncoder());
        pipeline.addLast(new RaftServerProtocolEncoder());
        pipeline.addLast(new RaftServerProtocolDecoder());
        pipeline.addLast(new ResponsePackageHandler());
        pipeline.addLast(new RaftServerProcessHandler());
        pipeline.addLast(loggingHandler);
    }
}
