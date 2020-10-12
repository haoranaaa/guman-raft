package com.guman.raft.constant;

/**
 * Created by guman on 2020/3/24.
 */
public enum ServerState {
    /**
     * 领导状态
     */
    LEADER,
    /**
     * 跟随者状态
     */
    FOLLOWER,
    /**
     * 中间状态
     * 当节点想要申请为leader时 需要先改变自身状态为该状态
     * 成功则变为leader 失败则返回follower
     */
    CANDIDATE;

}
