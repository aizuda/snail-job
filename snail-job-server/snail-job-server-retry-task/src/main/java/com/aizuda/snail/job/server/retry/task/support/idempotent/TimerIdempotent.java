package com.aizuda.snail.job.server.retry.task.support.idempotent;

import cn.hutool.core.lang.Pair;
import com.aizuda.snail.job.server.common.IdempotentStrategy;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

/**
 * @author opensnail
 * @date 2023-10-19 21:54:57
 * @since 2.4.0
 */
@Slf4j
public class TimerIdempotent implements IdempotentStrategy<Pair<String/*groupName*/, String/*namespaceId*/>, String> {

    private static final String KEY_FORMAT = "{0}_{1}_{2}";
    private static final Cache<String, String> cache;

    static {
        cache = CacheBuilder.newBuilder()
            .concurrencyLevel(16) // 并发级别
            .expireAfterWrite(20, TimeUnit.SECONDS)
            .build();
    }

    @Override
    public boolean set(Pair<String/*groupName*/, String/*namespaceId*/> pair, String value) {
        cache.put(getKey(pair, value), value);
        return Boolean.TRUE;
    }

    private static String getKey(Pair<String/*groupName*/, String/*namespaceId*/> pair, final String value) {
        return MessageFormat.format(KEY_FORMAT, pair.getKey(), pair.getValue(), value);
    }

    @Override
    public String get(Pair<String/*groupName*/, String/*namespaceId*/> pair) {
       throw new UnsupportedOperationException("不支持此操作");
    }

    @Override
    public boolean isExist(Pair<String/*groupName*/, String/*namespaceId*/> pair, String value) {
        return cache.asMap().containsKey(getKey(pair, value));
    }

    @Override
    public boolean clear(Pair<String/*groupName*/, String/*namespaceId*/> pair, String value) {
        cache.invalidate(getKey(pair, value));
        return Boolean.TRUE;
    }
}
