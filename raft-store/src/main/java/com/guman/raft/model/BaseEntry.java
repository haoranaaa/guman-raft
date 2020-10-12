package com.guman.raft.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by guman on 2020/3/24.
 */
@Data
public class BaseEntry implements Serializable {
    /**
     *
     * 候选 任期号
     */
    private long term;
    /**
     * 被请求者id
     */
    private int serviceId;

}
