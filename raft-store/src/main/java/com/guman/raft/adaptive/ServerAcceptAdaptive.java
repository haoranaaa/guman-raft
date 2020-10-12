package com.guman.raft.adaptive;

import com.guman.raft.http.annotation.AcceptHandler;
import com.guman.raft.http.annotation.AcceptType;
import com.guman.raft.http.api.AcceptMessageHandler;
import com.guman.raft.http.msg.RequestMessage;
import com.guman.raft.http.msg.body.OperationResult;

/**
 * @author duanhaoran
 * @since 2020/4/19 11:45 PM
 */
@AcceptHandler(AcceptType.SERVER_RESPONSE)
public class ServerAcceptAdaptive implements AcceptMessageHandler<RequestMessage> {

    @Override
    public OperationResult handler(RequestMessage message) {
        System.out.println("server reveive  msg ! ");
        return null;
    }
}
