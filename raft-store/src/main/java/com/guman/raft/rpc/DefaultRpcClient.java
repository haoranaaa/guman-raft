package com.guman.raft.rpc;

import com.guman.raft.exception.RaftRemoteException;
import com.guman.raft.http.RaftNettyClient;
import com.guman.raft.http.codec.dispatcher.OperationResultFeature;
import com.guman.raft.rpc.model.Request;
import com.guman.raft.rpc.model.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author duanhaoran
 * @since 2020/3/28 6:47 PM
 */
@Slf4j
public class DefaultRpcClient implements RpcClient {

    private static final Integer DEFAULT_TIME_OUT = 2000;

    private RaftNettyClient raftNettyClient;

    public DefaultRpcClient() {
        raftNettyClient = new RaftNettyClient();
        raftNettyClient.init();
    }

    @Override
    public Response send(Request request) {
        return this.send(request, DEFAULT_TIME_OUT);
    }

    @Override
    public Response send(Request request, int timeOut) {
        try {
            OperationResultFeature feature = raftNettyClient.sendMsg(request.getNodeId(), request);
            return (Response) feature.get(timeOut, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            log.error("read address error ! ", e);
            throw new RaftRemoteException();
        }
        return null;
    }

    @Override
    public Response sendAsync(Request request) {
        return null;
    }
}
