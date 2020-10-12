package com.guman.raft.model;

import lombok.Data;

import java.util.Objects;

/**
 * @author duanhaoran
 * @since 2020/3/28 8:32 PM
 */
@Data
public class Peer {
    private int nodeId;

    /** ip:selfPort */
    private String addr;

    private Integer port = 8080;

    public Peer(int nodeId) {
        this.nodeId = nodeId;
    }

    public Peer(int nodeId, String addr, Integer port) {
        this.nodeId = nodeId;
        this.addr = addr;
        this.port = port;
    }

    @Override
    public String toString() {
        return "Peer{" +
                "addr='" + addr + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peer peer = (Peer) o;
        return Objects.equals(addr, peer.addr) &&
                Objects.equals(port, peer.port);
    }

    @Override
    public int hashCode() {

        return Objects.hash(addr, port);
    }
}
