package com.guman.raft.http.codec.constant;

/**
 * @author duanhaoran
 * @since 2020/4/17 10:48 PM
 */
public class NettyConstant {

    public static final int RET_CONNECT_TIME_COUNT = 12;

    public static final int RET_CONNECT_INTERVAL_TIME = 12;

    public static final boolean RE_CONNECT_TIME = Boolean.TRUE;

    public static final int CLIENT_READ_IDEL_TIME_OUT = 3;

    public static final int CLIENT_WRITE_IDEL_TIME_OUT = 0;

    public static final int CLIENT_ALL_IDEL_TIME_OUT = 0;

    public static NodeLocal getNodeLocal(int nodeId) {
        for (NodeLocal nodeLocal : NodeLocal.values()) {
            if (nodeLocal.nodeId == nodeId) {
                return nodeLocal;
            }
        }

        return NodeLocal.node1;
    }

    public enum NodeLocal {
        /**
         *
         *
         */
        node1(1, "127.0.0.1", 8080),

        node2(2, "127.0.0.1", 8081),

        node3(3, "127.0.0.1", 8082);

        int nodeId;

        String host;

        int port;

        NodeLocal(int nodeId, String host, int port) {
            this.nodeId = nodeId;
            this.host = host;
            this.port = port;
        }

        public int getNodeId() {
            return nodeId;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }

}
