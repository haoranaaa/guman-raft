package com.guman.raft.http.codec.handler.client;

import com.guman.raft.http.annotation.AcceptType;
import com.guman.raft.http.codec.dispatcher.RequestPendingCenter;
import com.guman.raft.http.codec.handler.AbstractAcceptMsgAdaptive;
import com.guman.raft.http.msg.ResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author duanhaoran
 * @since 2020/4/15 12:50 AM
 */
@ChannelHandler.Sharable
public class ResponseDispatchHandler extends AbstractAcceptMsgAdaptive<ResponseMessage> {

    private RequestPendingCenter requestPendingCenter;

    public ResponseDispatchHandler(RequestPendingCenter requestPendingCenter) {
        this.requestPendingCenter = requestPendingCenter;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ResponseMessage responseMessage) throws Exception {
        if (getAcceptMsgAdaptive() != null) {
            getAcceptMsgAdaptive().handler(responseMessage);
        }
        requestPendingCenter.set(responseMessage.getMessageHeader().getStreamId(), responseMessage.getMessageBody());
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    protected AcceptType getAcceptType() {
        return AcceptType.CLIENT_RESPONSE;
    }
}
