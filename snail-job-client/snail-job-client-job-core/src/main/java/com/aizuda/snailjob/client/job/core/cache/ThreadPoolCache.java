package com.aizuda.snailjob.client.job.core.cache;

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
 * @author: opensnail
 * @date : 2023-09-27 17:12
 * @since : 2.4.0
 */
@Component
@Slf4j
public class ThreadPoolCache {
    private static final ConcurrentHashMap<Long, ThreadPoolExecutor> CACHE_THREAD_POOL = new ConcurrentHashMap<>();

    public static ThreadPoolExecutor createThreadPool(Long taskBatchId, int parallelNum) {
        if (CACHE_THREAD_POOL.containsKey(taskBatchId)) {
            ThreadPoolExecutor cacheThreadPool = CACHE_THREAD_POOL.get(taskBatchId);
            // 大于1说明已经更新了线程池的线程数，为了防止后面任务执行过程任务并行度改变影响已经产生的批次，这里不再做更新操作
            if (cacheThreadPool.getCorePoolSize() > 1) {
                return cacheThreadPool;
            }

            // 若能执行到这里只有分片任务(静态分片、MAP、MapReduce)才会需要多线程支持
            cacheThreadPool.setCorePoolSize(Math.min(parallelNum, cacheThreadPool.getMaximumPoolSize()));
            return cacheThreadPool;
        }

        Supplier<ThreadPoolExecutor> supplier = () -> {
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                    // 默认情况先只设置一个线程, 只有分片任务(静态分片、MAP、MapReduce)才会需要多线程支持
                    1, parallelNum, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                    new CustomizableThreadFactory(MessageFormat.format("snail-job-job-{0}-", taskBatchId)));
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
