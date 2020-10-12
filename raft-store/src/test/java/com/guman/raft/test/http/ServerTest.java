package com.guman.raft.test.http;

import com.google.common.collect.Lists;
import com.guman.raft.http.RaftNettyServer;
import com.guman.raft.http.handler.RaftServerInitialHandler;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author duanhaoran
 * @since 2020/4/20 4:30 PM
 */
public class ServerTest {

    public static void main(String[] args) throws InterruptedException {
        RaftNettyServer raftNettyServer = new RaftNettyServer();
        raftNettyServer.init(8099);
    }
}
