package com.guman.raft.http.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author duanhaoran
 * @since 2020/4/12 6:25 PM
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageHeader {

    private int version = 1;

    private int opCode;

    private long streamId;

    private int nodeId;
}
