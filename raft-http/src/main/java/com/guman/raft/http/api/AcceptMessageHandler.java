package com.guman.raft.http.api;

import com.guman.raft.http.msg.Message;
import com.guman.raft.http.msg.body.OperationResult;

/**
 * @author duanhaoran
 * @since 2020/4/19 11:26 PM
 */
public interface AcceptMessageHandler<T extends Message> {

    OperationResult handler(T message);

}
