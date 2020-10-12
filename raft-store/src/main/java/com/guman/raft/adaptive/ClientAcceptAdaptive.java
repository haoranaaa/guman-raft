package com.guman.raft.adaptive;

import com.guman.raft.http.annotation.AcceptHandler;
import com.guman.raft.http.annotation.AcceptType;
import com.guman.raft.http.api.AcceptMessageHandler;
import com.guman.raft.http.msg.ResponseMessage;
import com.guman.raft.http.msg.body.OperationResult;

/**
 * @author duanhaoran
 * @since 2020/4/19 11:45 PM
 */
@AcceptHandler(AcceptType.CLIENT_RESPONSE)
public class ClientAcceptAdaptive implements AcceptMessageHandler<ResponseMessage> {

    @Override
    public OperationResult handler(ResponseMessage message) {
        System.out.println("client reveive  msg ! ");
        return null;
    }
}
