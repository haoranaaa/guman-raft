package com.guman.raft.http.codec.handler.server;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * ByteToMessage Decode
 * 第一遍解码
 * @author duanhaoran
 * @since 2020/4/12 6:10 PM
 */
public class RaftFrameDecoder extends LengthFieldBasedFrameDecoder {
    public RaftFrameDecoder() {
        super(Integer.MAX_VALUE, 0, 2, 0, 2);
    }
}
