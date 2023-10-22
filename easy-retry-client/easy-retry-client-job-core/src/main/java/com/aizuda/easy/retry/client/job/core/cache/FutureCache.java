package com.aizuda.easy.retry.client.job.core.cache;

import com.aizuda.easy.retry.client.model.ExecuteResult;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author www.byteblogs.com
 * @date 2023-10-08 23:03:53
 * @since 2.4.0
 */
public class FutureCache {

    private static final ConcurrentHashMap<Long, ListenableFuture<ExecuteResult>> futureCache = new ConcurrentHashMap<>();

    public static void addFuture(Long taskBatchId, ListenableFuture<ExecuteResult> future) {
        futureCache.put(taskBatchId, future);
    }

    public static void remove(Long taskBatchId) {
        Optional.ofNullable(futureCache.get(taskBatchId)).ifPresent(future -> {
            future.cancel(true);
            futureCache.remove(taskBatchId);
        });

    }

}
