package com.guman.raft.http.codec.server;

import com.guman.raft.http.msg.RequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * messageToMessage
 * 第二遍解码
 * @author duanhaoran
 * @since 2020/4/12 6:12 PM
 */
public class RaftServerProtocolDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.decode(byteBuf);

        list.add(requestMessage);
    }
}
