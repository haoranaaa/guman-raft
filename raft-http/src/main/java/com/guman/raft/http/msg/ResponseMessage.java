package com.guman.raft.http.msg;

import com.guman.raft.http.msg.body.OperationResult;

/**
 * @author duanhaoran
 * @since 2020/4/12 6:48 PM
 */
public class ResponseMessage extends Message<OperationResult> {
    @Override
    public Class getMessageBodyDecodeClass(int opcode) {
        return OperationType.fromOpCode(opcode).getOperationResultClazz();
    }
}
