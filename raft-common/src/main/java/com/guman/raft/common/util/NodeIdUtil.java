package com.guman.raft.common.util;

/**
 * @author duanhaoran
 * @since 2020/4/23 3:25 PM
 */
public class NodeIdUtil {

    public static int getNodeId() {
        return Integer.valueOf(System.getProperty("nodeId", "1"));
    }
}
