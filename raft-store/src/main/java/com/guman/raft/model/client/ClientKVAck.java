package com.guman.raft.model.client;

import lombok.Builder;
import lombok.Data;

/**
 * @author duanhaoran
 * @since 2020/4/23 10:35 PM
 */
@Data
@Builder
public class ClientKVAck {
    Object result;

    public ClientKVAck(Object result) {
        this.result = result;
    }

    public static ClientKVAck ok() {
        return new ClientKVAck("ok");
    }

    public static ClientKVAck fail() {
        return new ClientKVAck("fail");
    }

}
