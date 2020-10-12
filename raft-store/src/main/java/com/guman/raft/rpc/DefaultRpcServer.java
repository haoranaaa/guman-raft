package com.guman.raft.rpc;

import com.guman.raft.exception.RaftRemoteException;
import com.guman.raft.http.RaftNettyServer;
import com.guman.raft.impl.DefaultNode;
import com.guman.raft.model.*;
import com.guman.raft.model.client.ClientPairReq;
import com.guman.raft.rpc.model.Request;
import com.guman.raft.rpc.model.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * @author duanhaoran
 * @since 2020/3/28 7:18 PM
 */
@Slf4j
public class DefaultRpcServer implements RpcServer {

    private RaftNettyServer raftNettyServer;

    private DefaultNode node;

    public DefaultRpcServer(int port, DefaultNode node) {
        this.node = node;
        try {
            raftNettyServer = new RaftNettyServer();
            raftNettyServer.init(port);
        } catch (InterruptedException e) {
            log.error("start server error !", e);
            throw new RaftRemoteException("start netty server error !");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response handleRequest(Request request) {
        switch (request.getCmd()) {
            case Request.R_VOTE:
                return new Response(node.handleRequestVote((RemoteParam) request.getObj()));
            case Request.A_ENTRIES:
                return new Response(node.handleRequestAppendLog((BaseEntryParams) request.getObj()));
            case Request.CLIENT_REQ:
                return new Response(node.handleClientRequest((ClientPairReq) request.getObj()));
            case Request.CHANGE_CONFIG_ADD:
            case Request.CHANGE_CONFIG_REMOVE:
            default:
                return null;
        }
    }

    @Override
    public void init() {
//        this.rpcServer.start();
    }

    @Override
    public void destroy() {
//        this.rpcServer.stop();
    }
}
