package com.guman.raft.http.codec.handler.server;

import com.guman.raft.http.annotation.AcceptType;
import com.guman.raft.http.codec.handler.AbstractAcceptMsgAdaptive;
import com.guman.raft.http.container.ChannelContainer;
import com.guman.raft.http.msg.RequestMessage;
import com.guman.raft.http.msg.body.OperationResult;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.java.Log;

/**
 * 请求接收端
 * 收到请求后的业务逻辑处理
 *
 * @author duanhaoran
 * @since 2020/4/12 6:57 PM
 */
@Log
public class RaftServerProcessHandler extends AbstractAcceptMsgAdaptive<RequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RequestMessage requestMessage) throws Exception {
        System.out.println(requestMessage.getMessageHeader().getStreamId());
        if (getAcceptMsgAdaptive() != null) {
            OperationResult result = getAcceptMsgAdaptive().handler(requestMessage);
            if (result != null) {
                channelHandlerContext.channel().writeAndFlush(result);
            }
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("channel registered ! " + ctx.channel().id());
        super.channelRegistered(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelContainer.unRegister(ctx.channel());
        log.info("channel inactive ! " + ctx.channel().id());
        super.channelInactive(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ChannelContainer.register(ctx.channel());
        log.info("channel active ! " + ctx.channel().id());
        super.channelActive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("channel unregistered ! " + ctx.channel().id());
        super.channelUnregistered(ctx);
    }

    @Override
    protected AcceptType getAcceptType() {
        return AcceptType.SERVER_RESPONSE;
    }
}
