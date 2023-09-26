package com.aizuda.easy.retry.client.job.core.cache;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-27 17:12
 */
@Component
public class ThreadPoolCache {
    private static final ConcurrentHashMap<Long, ThreadPoolExecutor> CACHE_THREAD_POOL = new ConcurrentHashMap<>();

    public static ThreadPoolExecutor createThreadPool(Long taskId, int parallelNum) {
        Supplier<ThreadPoolExecutor> supplier = () -> new ThreadPoolExecutor(
                parallelNum, parallelNum, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );
        return CACHE_THREAD_POOL.putIfAbsent(taskId, supplier.get());
    }

    public static void getThreadPool(Long taskId, int parallelNum) {


    }
}
