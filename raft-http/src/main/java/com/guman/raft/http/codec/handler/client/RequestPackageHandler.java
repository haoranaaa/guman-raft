package com.guman.raft.http.codec.handler.client;

import com.guman.raft.common.util.IdGenUtil;
import com.guman.raft.http.msg.RequestMessage;
import com.guman.raft.http.msg.body.Operation;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * @author duanhaoran
 * @since 2020/4/15 6:26 PM
 */
public class RequestPackageHandler extends MessageToMessageEncoder<Operation> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Operation operation, List<Object> list) throws Exception {
        RequestMessage requestMessage = new RequestMessage(IdGenUtil.gen(), operation);
        list.add(requestMessage);
    }
}
