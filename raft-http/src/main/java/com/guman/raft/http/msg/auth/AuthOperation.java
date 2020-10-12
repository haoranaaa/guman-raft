package com.guman.raft.http.msg.auth;

import com.guman.raft.http.msg.body.Operation;
import com.guman.raft.http.msg.body.OperationResult;
import lombok.Data;
import lombok.extern.java.Log;

/**
 * @author duanhaoran
 * @since 2020/4/12 6:38 PM
 */
@Data
@Log
public class AuthOperation extends Operation {

    private String userName;

    private String password;
}
