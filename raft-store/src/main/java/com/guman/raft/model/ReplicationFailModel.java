package com.guman.raft.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.Callable;

/**
 *
 * @author duanhaoran
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReplicationFailModel {
    static String count = "_count";
    static String success = "_success";

    public String countKey;
    public String successKey;
    public Callable callable;
    public EntryParam logEntry;
    public Peer peer;
    public Long offerTime;

    public ReplicationFailModel(Callable callable, EntryParam logEntry, Peer peer, Long offerTime) {
        this.callable = callable;
        this.logEntry = logEntry;
        this.peer = peer;
        this.offerTime = offerTime;
        countKey = logEntry.getPair().getKey() + count;
        successKey = logEntry.getPair().getKey() + success;
    }

}
