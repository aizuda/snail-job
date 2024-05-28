package com.aizuda.snailjob.server.retry.task.support.idempotent;

import com.aizuda.snailjob.server.common.IdempotentStrategy;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author opensnail
 * @date 2023-10-19 21:54:57
 * @since 2.4.0
 */
public class TimerIdempotent implements IdempotentStrategy<String> {

    private static final Cache<String, String> cache;

    static {
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(16) // 并发级别
                .expireAfterWrite(20, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean set(String key) {
        cache.put(key, key);
        return Boolean.TRUE;
    }

    @Override
    public boolean isExist(String key) {
        return cache.asMap().containsKey(key);
    }

    @Override
    public boolean clear(String key) {
        cache.invalidate(key);
        return Boolean.TRUE;
    }
}
