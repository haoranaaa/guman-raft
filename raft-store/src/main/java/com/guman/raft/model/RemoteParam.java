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
public class RemoteParam extends BaseEntry{

    /** 请求选票的候选人的 Id(ip:selfPort) */
    int candidateId;

    /** 候选人的最后日志条目的索引值 */
    long lastLogIndex;

    /** 候选人最后日志条目的任期号  */
    long lastLogTerm;

}
