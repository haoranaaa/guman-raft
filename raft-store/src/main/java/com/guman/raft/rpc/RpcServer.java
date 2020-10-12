package com.guman.raft.rpc;

import com.guman.raft.LifeCycle;
import com.guman.raft.rpc.model.Request;
import com.guman.raft.rpc.model.Response;

/**
 * @author duanhaoran
 * @since 2020/3/28 7:17 PM
 */
public interface RpcServer extends LifeCycle {

    /**
     * 处理请求
     */
    Response handleRequest(Request request);
}
