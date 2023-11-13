package com.aizuda.easy.retry.server.retry.task.support.idempotent;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.server.common.IdempotentStrategy;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author www.byteblogs.com
 * @date 2023-10-19 21:54:57
 * @since 2.4.0
 */
@Slf4j
public class TimerIdempotent implements IdempotentStrategy<String, String> {

    private static final Cache<String, String> cache;

    static {
        cache = CacheBuilder.newBuilder()
            .concurrencyLevel(16) // 并发级别
            .expireAfterWrite(20, TimeUnit.SECONDS)
            .build();
    }

    @Override
    public boolean set(String key, String value) {
        cache.put(getKey(key, value), value);
        return Boolean.TRUE;
    }

    private static String getKey(final String key, final String value) {
        return key.concat(StrUtil.UNDERLINE).concat(value);
    }

    @Override
    public String get(String s) {
       throw new UnsupportedOperationException("不支持此操作");
    }

    @Override
    public boolean isExist(String key, String value) {
        return cache.asMap().containsKey(getKey(key, value));
    }

    @Override
    public boolean clear(String key, String value) {
        cache.invalidate(getKey(key, value));
        return Boolean.TRUE;
    }
}
