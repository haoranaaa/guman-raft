package com.guman.raft.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by guman on 2020/3/24.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoteResult implements Serializable {

    /** 
     * 当前任期号，以便于候选人去更新自己的任期 
     */
    long term;

    /**
     * 候选人赢得了此张选票时为真 
     */
    boolean voteGranted;

    public VoteResult(boolean voteGranted) {
        this.voteGranted = voteGranted;
    }

    public static VoteResult fail() {
        return new VoteResult(false);
    }

    public static VoteResult ok() {
        return new VoteResult(true);
    }

}
