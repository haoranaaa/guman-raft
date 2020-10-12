package com.guman.raft.rpc;

import com.guman.raft.rpc.model.Request;
import com.guman.raft.rpc.model.Response;

/**
 * @author duanhaoran
 * @since 2020/3/28 6:43 PM
 */
public interface RpcClient {

    Response send(Request request);

    Response send(Request request, int timeOut);

    Response sendAsync(Request request);
}
