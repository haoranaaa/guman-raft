package com.guman.raft.http.msg;

import com.guman.raft.http.msg.body.Operation;

/**
 * @author duanhaoran
 * @since 2020/4/12 6:13 PM
 */
public class RequestMessage extends Message<Operation> {

    @Override
    public Class getMessageBodyDecodeClass(int opcode) {
        return OperationType.fromOpCode(opcode).getOperationClazz();
    }

    public RequestMessage() {
    }

    public RequestMessage(Long streamId, Operation operation){
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setStreamId(streamId);
        messageHeader.setNodeId(Integer.valueOf(System.getProperty("nodeId", "1")));
        messageHeader.setOpCode(OperationType.fromOperation(operation).getOpCode());
        this.setMessageHeader(messageHeader);
        this.setMessageBody(operation);
    }
}
