package com.aizuda.snailjob.server.job.task.support.idempotent;

import com.aizuda.snailjob.server.common.IdempotentStrategy;
import com.aizuda.snailjob.server.common.triple.Pair;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author opensnail
 * @date 2023-10-19 21:54:57
 * @since 2.4.0
 */
public class TimerIdempotent implements IdempotentStrategy<Integer, Long> {

    private static final Cache<Pair<Integer/*任务类型: SyetemTaskTypeEnum*/, Long /*批次id*/>, Long> cache;

    static {
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(8) // 并发级别
                // 设置过期时间避免由于异常情况导致时间轮的缓存没有删除
                .expireAfterWrite(20, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean set(Integer type, Long value) {
        cache.put(getKey(type, value), value);
        return Boolean.TRUE;
    }

    @Override
    public Long get(Integer s) {
        throw new UnsupportedOperationException("不支持此操作");
    }

    @Override
    public boolean isExist(Integer type, Long value) {
        return cache.asMap().containsKey(getKey(type, value));
    }

    @Override
    public boolean clear(Integer type, Long value) {
        cache.invalidate(getKey(type, value));
        return Boolean.TRUE;
    }

    private static Pair<Integer, Long> getKey(Integer type, Long value) {
        return Pair.of(type, value);
    }
}
