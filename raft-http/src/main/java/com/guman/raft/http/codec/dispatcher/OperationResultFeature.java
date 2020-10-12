package com.guman.raft.http.codec.dispatcher;

import com.guman.raft.http.msg.body.OperationResult;

import java.util.concurrent.CompletableFuture;

/**
 * @author duanhaoran
 * @since 2020/4/15 12:25 AM
 */
public class OperationResultFeature extends CompletableFuture<OperationResult> {

    public static OperationResultFeature fail() {
        OperationResultFeature operationResultFeature = new OperationResultFeature();
        operationResultFeature.completeExceptionally(new RuntimeException());
        return operationResultFeature;
    }
}
