package com.guman.raft.http.msg;

import com.guman.raft.http.msg.auth.AuthOperation;
import com.guman.raft.http.msg.auth.AuthOperationResult;
import com.guman.raft.http.msg.body.Operation;
import com.guman.raft.http.msg.body.OperationResult;
import com.guman.raft.http.msg.keepalive.KeepAliveOperation;
import com.guman.raft.http.msg.keepalive.KeepaliveOperationResult;
import com.guman.raft.http.msg.raft.RaftOperation;
import com.guman.raft.http.msg.raft.RaftOperationResult;

import java.util.function.Predicate;

/**
 * @author duanhaoran
 * @since 2020/4/12 6:34 PM
 */
public enum OperationType {
    /**
     * 认证
     */
    AUTH(1, AuthOperation.class, AuthOperationResult.class),
    /**
     * 长链接
     */
    KEEP_ALIVE(2, KeepAliveOperation.class, KeepaliveOperationResult.class),
    /**
     * 主动消息
     */
    MSG(3, RaftOperation.class, RaftOperationResult.class);

    private int opCode;
    private Class<? extends Operation> operationClazz;
    private Class<? extends OperationResult> operationResultClazz;

    OperationType(int opCode, Class<? extends Operation> operationClazz, Class<? extends OperationResult> responseClass) {
        this.opCode = opCode;
        this.operationClazz = operationClazz;
        this.operationResultClazz = responseClass;
    }

    public int getOpCode(){
        return opCode;
    }

    public Class<? extends Operation> getOperationClazz() {
        return operationClazz;
    }

    public Class<? extends OperationResult> getOperationResultClazz() {
        return operationResultClazz;
    }

    public static OperationType fromOpCode(int type){
        return getOperationType(requestType -> requestType.opCode == type);
    }

    public static OperationType fromOperation(Operation operation){
        return getOperationType(requestType -> requestType.operationClazz == operation.getClass());
    }

    private static OperationType getOperationType(Predicate<OperationType> predicate){
        OperationType[] values = values();
        for (OperationType operationType : values) {
            if(predicate.test(operationType)){
                return operationType;
            }
        }

        throw new AssertionError("no found type");
    }

}
