package com.guman.raft.http.msg;

import com.guman.raft.common.util.IdGenUtil;
import com.guman.raft.http.msg.keepalive.KeepAliveOperation;

/**
 * @author duanhaoran
 * @since 2020/4/17 11:38 PM
 */
public class MessageGenUtil {

    public static RequestMessage genKeepAliveRequest() {
        return new RequestMessage(IdGenUtil.gen(), new KeepAliveOperation());
    }
}
