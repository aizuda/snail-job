package com.aizuda.snailjob.server.job.task.support.idempotent;

import com.aizuda.snailjob.server.common.IdempotentStrategy;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

/**
 * @author opensnail
 * @date 2023-10-19 21:54:57
 * @since 2.4.0
 */
public class TimerIdempotent implements IdempotentStrategy<Integer, Long> {
    private static final String KEY_FORMAT = "{0}_{1}_{2}";

    private static final Cache<String, Long> cache;

    static {
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(8) // 并发级别
                // 设置过期时间避免由于异常情况导致时间轮的缓存没有删除
                .expireAfterWrite(20, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean set(Integer key, Long value) {
        cache.put(getKey(key, value), value);
        return Boolean.TRUE;
    }

    @Override
    public Long get(Integer s) {
        throw new UnsupportedOperationException("不支持此操作");
    }

    @Override
    public boolean isExist(Integer key, Long value) {
        return cache.asMap().containsKey(getKey(key, value));
    }

    @Override
    public boolean clear(Integer key, Long value) {
        cache.invalidate(getKey(key, value));
        return Boolean.TRUE;
    }

    private static String getKey(Integer key, Long value) {
        return MessageFormat.format(KEY_FORMAT, key, value);
    }
}
