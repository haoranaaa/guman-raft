package com.guman.raft.rpc.model;

import com.guman.raft.http.msg.raft.RaftOperationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author duanhaoran
 * @since 2020/3/28 6:45 PM
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Response<T> extends RaftOperationResult implements Serializable {

    private int code;

    private T result;

    public Response(T result) {
        this(0, result);
    }
}
