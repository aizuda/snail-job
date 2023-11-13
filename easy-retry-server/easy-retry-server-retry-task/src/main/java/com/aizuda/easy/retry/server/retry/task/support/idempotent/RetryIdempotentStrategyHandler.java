package com.aizuda.easy.retry.server.retry.task.support.idempotent;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.server.common.IdempotentStrategy;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 重试任务幂等校验器
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-23 09:26
 */
@Component
public class RetryIdempotentStrategyHandler implements IdempotentStrategy<String, Long> {
    private static final Cache<String, Long> cache;

    static {
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(16) // 并发级别
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean set(String groupId, Long value) {
        cache.put(getKey(groupId, value), value);
        return Boolean.TRUE;
    }

    @Override
    public Long get(String s) {
        throw new EasyRetryServerException("不支持的操作");
    }

    @Override
    public boolean isExist(String groupId, Long value) {
        return cache.asMap().containsKey(getKey(groupId, value));
    }

    @Override
    public boolean clear(String groupId, Long value) {
        cache.invalidate(getKey(groupId, value));
        return Boolean.TRUE;
    }

    private static String getKey(final String key, final Long value) {
        return key.concat(StrUtil.UNDERLINE).concat(String.valueOf(value));
    }
}
