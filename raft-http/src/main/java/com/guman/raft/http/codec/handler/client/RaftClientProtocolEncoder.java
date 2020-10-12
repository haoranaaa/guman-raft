package com.guman.raft.http.codec.handler.client;

import com.guman.raft.http.msg.RequestMessage;
import com.guman.raft.http.msg.ResponseMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * @author duanhaoran
 * @since 2020/4/12 7:04 PM
 */
public class RaftClientProtocolEncoder extends MessageToMessageEncoder<RequestMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RequestMessage requestMessage, List<Object> list) throws Exception {
        ByteBuf buffer = channelHandlerContext.alloc().buffer();
        requestMessage.encode(buffer);
        list.add(buffer);
    }
}
