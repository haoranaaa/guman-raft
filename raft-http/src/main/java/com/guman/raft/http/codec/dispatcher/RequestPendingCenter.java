package com.guman.raft.http.codec.dispatcher;

import com.google.common.collect.Maps;
import com.guman.raft.http.msg.body.OperationResult;

import java.util.Map;

/**
 * @author duanhaoran
 * @since 2020/4/15 12:27 AM
 */
public class RequestPendingCenter {

    private Map<Long, OperationResultFeature> map = Maps.newConcurrentMap();

    public void add(Long streamId, OperationResultFeature feature) {
        map.put(streamId, feature);
    }

    public OperationResultFeature get(Long streamId) {
        return map.get(streamId);
    }

    public void set(Long streamId, OperationResult result) {
        OperationResultFeature operationResultFeature = map.get(streamId);
        if (operationResultFeature == null) {
            return;
        }
        operationResultFeature.complete(result);
        map.remove(streamId);
    }


}
