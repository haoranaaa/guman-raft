package com.guman.raft;

import com.guman.raft.model.EntryParam;

/**
 * @author duanhaoran
 * @since 2020/3/28 6:35 PM
 */
public interface Store {

    /**
     * 数据存储
     * @param logEntry 日志中的数据.
     */
    void apply(EntryParam logEntry);

    EntryParam get(String key);

    String getString(String key);

    void setString(String key, String value);

    void delString(String... key);
}
