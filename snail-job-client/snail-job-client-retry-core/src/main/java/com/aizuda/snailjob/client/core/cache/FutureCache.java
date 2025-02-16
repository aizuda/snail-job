package com.aizuda.snailjob.client.core.cache;

import com.aizuda.snailjob.client.model.DispatchRetryResultDTO;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author opensnail
 * @date 2023-10-08 23:03:53
 * @since 2.4.0
 */
public class FutureCache {

    private static final ConcurrentHashMap<Long, ListenableFuture<?>> futureCache = new ConcurrentHashMap<>();

    public static void addFuture(Long retryTaskId, ListenableFuture<?> future) {
        futureCache.put(retryTaskId, future);
    }

    public static void remove(Long taskBatchId) {
        Optional.ofNullable(futureCache.get(taskBatchId)).ifPresent(future -> {
            future.cancel(true);
            futureCache.remove(taskBatchId);
        });
    }

}
