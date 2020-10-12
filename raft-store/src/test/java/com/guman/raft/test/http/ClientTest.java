package com.guman.raft.test.http;

import com.guman.raft.http.RaftNettyClient;
import com.guman.raft.http.RaftNettyServer;
import com.guman.raft.http.handler.RaftServerInitialHandler;
import com.guman.raft.http.msg.raft.RaftOperation;

/**
 * @author duanhaoran
 * @since 2020/4/20 4:30 PM
 */
public class ClientTest {

    public static void main(String[] args) throws InterruptedException {
        RaftNettyClient raftNettyClient = new RaftNettyClient();
        raftNettyClient.connect(1);
        while (true) {
            Thread.sleep(1000);
            raftNettyClient.sendMsg(0, new RaftOperation());
        }
    }
}
