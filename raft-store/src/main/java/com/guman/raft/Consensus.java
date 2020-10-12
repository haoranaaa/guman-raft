package com.guman.raft;

import com.guman.raft.model.BaseEntryParams;
import com.guman.raft.model.EntryResult;
import com.guman.raft.model.RemoteParam;
import com.guman.raft.model.VoteResult;

/**
 * Created by guman on 2020/3/24.
 */
public interface Consensus {
    /**
     * 请求投票
     * @param param
     */
    VoteResult requestForVote(RemoteParam param);

    /**
     * 追加数据日志
     * @param param
     */
    EntryResult appendLogEntries(BaseEntryParams param);

}
