package com.aizuda.easy.retry.server.retry.task.support.idempotent;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.server.common.IdempotentStrategy;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

/**
 * 重试任务幂等校验器
 *
 * @author: opensnail
 * @date : 2021-11-23 09:26
 */
@Component
public class RetryIdempotentStrategyHandler implements IdempotentStrategy<Pair<String/*groupName*/, String/*namespaceId*/>, Long> {
    private static final String KEY_FORMAT = "{0}_{1}_{2}";

    private static final Cache<String, Long> cache;

    static {
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(16) // 并发级别
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean set(Pair<String/*groupName*/, String/*namespaceId*/> pair, Long value) {
        cache.put(getKey(pair, value), value);
        return Boolean.TRUE;
    }

    @Override
    public Long get(Pair<String/*groupName*/, String/*namespaceId*/> pair) {
        throw new EasyRetryServerException("不支持的操作");
    }

    @Override
    public boolean isExist(Pair<String/*groupName*/, String/*namespaceId*/> pair, Long value) {
        return cache.asMap().containsKey(getKey(pair, value));
    }

    @Override
    public boolean clear(Pair<String/*groupName*/, String/*namespaceId*/> pair, Long value) {
        cache.invalidate(getKey(pair, value));
        return Boolean.TRUE;
    }

    private static String getKey(Pair<String/*groupName*/, String/*namespaceId*/> pair, final Long value) {
        return MessageFormat.format(KEY_FORMAT, pair.getKey(), pair.getValue(), value);
    }
}
