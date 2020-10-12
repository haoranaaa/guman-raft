package com.guman.raft.http.msg.keepalive;

import com.guman.raft.http.msg.body.OperationResult;
import lombok.Data;
import lombok.extern.java.Log;

/**
 * @author duanhaoran
 * @since 2020/4/12 6:43 PM
 */
@Data
@Log
public class KeepaliveOperationResult extends OperationResult {

    private final long time;

}
