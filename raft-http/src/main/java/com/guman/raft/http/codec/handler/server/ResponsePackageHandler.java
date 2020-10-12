package com.guman.raft.http.codec.handler.server;

import com.guman.raft.common.util.IdGenUtil;
import com.guman.raft.common.util.NodeIdUtil;
import com.guman.raft.http.msg.MessageHeader;
import com.guman.raft.http.msg.OperationType;
import com.guman.raft.http.msg.ResponseMessage;
import com.guman.raft.http.msg.body.OperationResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * @author duanhaoran
 * @since 2020/4/21 3:17 PM
 */
public class ResponsePackageHandler extends MessageToMessageEncoder<OperationResult> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, OperationResult operationResult, List<Object> list) throws Exception {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessageHeader(new MessageHeader(1, OperationType.MSG.getOpCode(), IdGenUtil.gen(), NodeIdUtil.getNodeId()));
        responseMessage.setMessageBody(operationResult);
        list.add(responseMessage);
    }
}
