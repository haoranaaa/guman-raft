package com.guman.raft.common;

import com.google.common.collect.Lists;
import com.guman.raft.common.util.NodeIdUtil;
import com.guman.raft.http.codec.constant.NettyConstant;
import lombok.Data;

import java.util.List;

/**
 * @author duanhaoran
 * @since 2020/3/28 7:02 PM
 */
@Data
public class NodeConfig {
    /**
     * 当前节点端口号
     */
    private int selfPort;

    /**
     * 当前节点地址
     */
    private String selfAddress = "127.0.0.1";

    /**
     * 其它节点地址
     */
    private List<String> otherAddresses;
    /**
     * 当前节点nodeId
     */
    private int nodeId;

    public NodeConfig() {
        List<String> otherAddresses = Lists.newArrayList();
        int nodeId = NodeIdUtil.getNodeId();
        for (NettyConstant.NodeLocal nodeLocal : NettyConstant.NodeLocal.values()) {
            if (nodeId == nodeLocal.getNodeId()) {
                this.selfPort = nodeLocal.getPort();
                this.selfAddress = nodeLocal.getHost();
            } else {
                otherAddresses.add(nodeId + ":" + nodeLocal.getHost() + ":" + nodeLocal.getPort());
            }
        }
        this.otherAddresses = otherAddresses;
    }
}
