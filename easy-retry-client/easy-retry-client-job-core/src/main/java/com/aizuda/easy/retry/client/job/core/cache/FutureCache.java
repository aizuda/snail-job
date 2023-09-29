package com.aizuda.easy.retry.client.job.core.cache;

import com.aizuda.easy.retry.client.job.core.dto.JobExecutorInfo;
import com.aizuda.easy.retry.client.model.ExecuteResult;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author www.byteblogs.com
 * @date 2023-10-08 23:03:53
 * @since 2.4.0
 */
public class FutureCache {

    private static final Table<Long, Long, ListenableFuture<ExecuteResult>> futureCache = HashBasedTable.create();

    public static void addFuture(Long taskBatchId, Long taskId, ListenableFuture<ExecuteResult> future) {
        futureCache.put(taskBatchId, taskId, future);
    }

    public static void remove(Long taskBatchId, Long taskId) {
        ListenableFuture<ExecuteResult> future = futureCache.get(taskBatchId, taskId);
        future.cancel(true);
        futureCache.remove(taskBatchId, taskId);
    }

    public static void remove(Long taskBatchId) {
        Map<Long, ListenableFuture<ExecuteResult>> futureMap = futureCache.row(taskBatchId);
        futureMap.forEach((taskId, future) -> {
            future.cancel(true);
            futureCache.remove(taskBatchId, taskId);
        });
    }

}
