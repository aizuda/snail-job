package com.aizuda.easy.retry.client.job.core.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 *
 * @author: www.byteblogs.com
 * @date : 2023-09-27 17:12
 * @since : 2.4.0
 */
@Component
@Slf4j
public class ThreadPoolCache {
    private static final ConcurrentHashMap<Long, ThreadPoolExecutor> CACHE_THREAD_POOL = new ConcurrentHashMap<>();

    public static ThreadPoolExecutor createThreadPool(Long taskBatchId, int parallelNum) {
        if (CACHE_THREAD_POOL.containsKey(taskBatchId)) {
            return CACHE_THREAD_POOL.get(taskBatchId);
        }

        Supplier<ThreadPoolExecutor> supplier = () -> {

            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                    parallelNum, parallelNum, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                    new CustomizableThreadFactory(MessageFormat.format("easy-retry-job-{0}-", taskBatchId)));
            threadPoolExecutor.allowCoreThreadTimeOut(true);
            return threadPoolExecutor;
         };

        ThreadPoolExecutor threadPoolExecutor = supplier.get();
        CACHE_THREAD_POOL.putIfAbsent(taskBatchId, threadPoolExecutor);
        return threadPoolExecutor;
    }

    public static ThreadPoolExecutor getThreadPool(Long taskBatchId) {
        return CACHE_THREAD_POOL.get(taskBatchId);
    }

    public static void stopThreadPool(Long taskBatchId) {
        FutureCache.remove(taskBatchId);
        ThreadPoolExecutor threadPoolExecutor = CACHE_THREAD_POOL.get(taskBatchId);
        if (Objects.isNull(threadPoolExecutor)) {
            return;
        }

        threadPoolExecutor.shutdownNow();
        CACHE_THREAD_POOL.remove(taskBatchId);

    }
}
