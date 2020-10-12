package com.guman.raft.http.codec.handler.client;

import com.guman.raft.http.codec.constant.NettyConstant;
import com.guman.raft.http.codec.hoder.ChannelHandlerHolder;
import com.guman.raft.http.msg.MessageGenUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import lombok.extern.java.Log;

import java.util.concurrent.TimeUnit;

/**
 * @author duanhaoran
 * @since 2020/4/17 10:30 PM
 * 重连检测狗，当发现当前的链路不稳定关闭之后，进行12次重连
 */
@ChannelHandler.Sharable
@Log
public class ConnectionWatchdog extends ChannelInboundHandlerAdapter implements TimerTask, ChannelHandlerHolder {

    private final Bootstrap bootstrap;

    private final Timer timer;

    private final int nodeId;

    private int attempts = 0;

    public ConnectionWatchdog(Bootstrap bootstrap, Timer timer, int nodeId) {
        this.bootstrap = bootstrap;
        this.timer = timer;
        this.nodeId = nodeId;
    }

    /**
     * channel链路每次active的时候，将其连接的次数重新☞ 0
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("-------服务端上线----------");
        attempts = 0;
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("-------服务端下线----------");
        if (NettyConstant.RE_CONNECT_TIME && !ctx.channel().isActive()) {
            log.info("链接关闭，将进行重连");
            if (attempts < NettyConstant.RET_CONNECT_TIME_COUNT) {
                timer.newTimeout(this, NettyConstant.RET_CONNECT_INTERVAL_TIME, TimeUnit.SECONDS);
            }
        } else {
            log.info("链接关闭");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent stateEvent = (IdleStateEvent) evt;
        switch (stateEvent.state()) {
            case READER_IDLE:
                ctx.channel().writeAndFlush(MessageGenUtil.genKeepAliveRequest());
                break;
            case WRITER_IDLE:
                ctx.channel().writeAndFlush(MessageGenUtil.genKeepAliveRequest());
                break;
            default:
                break;
        }
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        ChannelFuture future;

        synchronized (bootstrap) {
            NettyConstant.NodeLocal nodeLocal = NettyConstant.getNodeLocal(nodeId);
            future = bootstrap.connect(nodeLocal.getHost(), nodeLocal.getPort());
        }

        future.addListener((ChannelFutureListener) f -> {
            boolean succeed = f.isSuccess();
            if (!succeed) {
                log.info("重连失败");
                attempts++;
                f.channel().pipeline().fireChannelInactive();
            } else {
                log.info("重连成功");
            }
        });
    }
}
