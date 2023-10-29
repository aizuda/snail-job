package com.aizuda.easy.retry.server.job.task.support.idempotent;

import com.aizuda.easy.retry.server.common.IdempotentStrategy;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

/**
 * @author www.byteblogs.com
 * @date 2023-10-19 21:54:57
 * @since 2.4.0
 */
public class TimerIdempotent implements IdempotentStrategy<Long, Long> {

    private static final Cache<Long, Long> cache;

    static {
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(8) // 并发级别
                // 设置过期时间避免由于异常情况导致时间轮的缓存没有删除
                .expireAfterWrite(20, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean set(Long key, Long value) {
        cache.put(key, value);
        return Boolean.TRUE;
    }

    @Override
    public Long get(Long s) {
        throw new UnsupportedOperationException("不支持此操作");
    }

    @Override
    public boolean isExist(Long key, Long value) {
        return cache.asMap().containsKey(key);
    }

    @Override
    public boolean clear(Long key, Long value) {
        cache.invalidate(key);
        return Boolean.TRUE;
    }
}
