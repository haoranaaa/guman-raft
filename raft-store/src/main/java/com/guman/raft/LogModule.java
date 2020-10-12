package com.guman.raft;

import com.guman.raft.model.EntryParam;

/**
 * @author duanhaoran
 * @since 2020/3/28 8:27 PM
 */
public interface LogModule {

    void write(EntryParam logEntry);

    EntryParam read(Long index);

    void removeOnStartIndex(Long startIndex);

    EntryParam getLast();

    Long getLastIndex();
}
