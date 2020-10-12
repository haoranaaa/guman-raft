package com.guman.raft.http.codec.handler.server;

import io.netty.handler.codec.LengthFieldPrepender;

/**
 * ByteToMessage Decode
 * 第一遍解码
 * @author duanhaoran
 * @since 2020/4/12 6:10 PM
 */
public class RaftFrameEncoder extends LengthFieldPrepender {

    public RaftFrameEncoder() {
        super(2);
    }
}
