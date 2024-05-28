package com.aizuda.snailjob.server.retry.task.support.idempotent;

import com.aizuda.snailjob.server.common.IdempotentStrategy;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 重试任务幂等校验器
 *
 * @author: opensnail
 * @date : 2021-11-23 09:26
 */
@Component
public class RetryIdempotentStrategyHandler implements IdempotentStrategy<String> {

    private static final Cache<String, String> cache;

    static {
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(16) // 并发级别
                .expireAfterWrite(60, TimeUnit.SECONDS)
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
