package com.guman.raft;

import com.guman.raft.impl.DefaultNode;

/**
 * @author duanhaoran
 * @since 2020/3/28 7:02 PM
 */
public class GumanRaftBootStrap {

    public static void main(String[] args) throws Throwable {
        main0();
    }

    public static void main0() throws Throwable {
        Node node = DefaultNode.getInstance();
        node.setConfig(null);

        node.init();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                node.destroy();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }));

    }

}
