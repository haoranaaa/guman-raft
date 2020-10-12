package com.guman.raft.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by guman on 2020/3/24.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EntryResult {
    /**
     * 当前的任期号，用于领导人去更新自己
     */
    long term;

    /**
     * 跟随者包含了匹配上 prevLogIndex 和 prevLogTerm 的日志时为真
     */
    boolean success;

    public EntryResult(boolean success) {
        this.success = success;
    }

    public static EntryResult fail() {
        return new EntryResult(false);
    }

    public static EntryResult success() {
        return new EntryResult(true);
    }
}
