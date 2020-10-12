package com.guman.raft.http.msg.auth;

import com.guman.raft.http.msg.body.OperationResult;
import lombok.Data;
import lombok.extern.java.Log;

/**
 * @author duanhaoran
 * @since 2020/4/12 6:39 PM
 */
@Data
@Log
public class AuthOperationResult extends OperationResult {

    private boolean authSuccess;

    public AuthOperationResult(boolean authSuccess) {
        this.authSuccess = authSuccess;
    }
}
