package com.aizuda.easy.retry.server.job.task.support.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author opensnail
 * @date 2023-10-21 23:35:42
 * @since 2.4.0
 */
public class ResidentTaskCache {

    private static final Cache<Long, Long/*ms*/> cache;

    static {
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(8) // 并发级别
                .expireAfterWrite(10, TimeUnit.SECONDS) // 写入后的过期时间
                .build();
    }

    public static void refresh(Long jobId, Long nextTriggerTime) {
        cache.put(jobId, nextTriggerTime);
    }

    public static Long getOrDefault(Long jobId, Long nextTriggerTime) {
        return Optional.ofNullable(cache.getIfPresent(jobId)).orElse(nextTriggerTime);
    }

    public static Long get(Long jobId) {
        return getOrDefault(jobId, null);
    }

    public static boolean isResident(Long jobId) {
        return cache.asMap().containsKey(jobId);
    }

}
