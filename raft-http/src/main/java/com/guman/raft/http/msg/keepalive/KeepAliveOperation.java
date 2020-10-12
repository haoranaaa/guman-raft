package com.guman.raft.http.msg.keepalive;

import com.guman.raft.http.msg.body.Operation;
import com.guman.raft.http.msg.body.OperationResult;
import lombok.Data;
import lombok.extern.java.Log;

/**
 * @author duanhaoran
 * @since 2020/4/12 6:40 PM
 */
@Data
@Log
public class KeepAliveOperation extends Operation {

    private long time;

    public KeepAliveOperation() {
        this.time = System.nanoTime();
    }
}
