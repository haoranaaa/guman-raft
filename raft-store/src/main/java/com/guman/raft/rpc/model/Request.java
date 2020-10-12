package com.guman.raft.rpc.model;

import com.guman.raft.http.msg.raft.RaftOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author duanhaoran
 * @since 2020/3/28 6:43 PM
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Request<T> extends RaftOperation implements Serializable {

    /** 请求投票 */
    public static final int R_VOTE = 0;
    /** 附加日志 */
    public static final int A_ENTRIES = 1;
    /** 客户端 */
    public static final int CLIENT_REQ = 2;
    /** 配置变更. add*/
    public static final int CHANGE_CONFIG_ADD = 3;
    /** 配置变更. remove*/
    public static final int CHANGE_CONFIG_REMOVE = 4;

    private int cmd = -1;

    private int nodeId;

    private T obj;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Request{");
        sb.append("cmd=").append(cmd);
        sb.append(", obj=").append(obj);
        sb.append('}');
        return sb.toString();
    }
}
