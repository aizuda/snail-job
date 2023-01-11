package com.x.retry.server.support.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.server.support.Lifecycle;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 缓存组组限流组件
 *
 * @author www.byteblogs.com
 * @date 2022-21-58
 * @since 2.0
 */
@Component
@Data
@Slf4j
public class CacheGroupRateLimiter implements Lifecycle {

    private static Cache<String, RateLimiter> CACHE;

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static Cache<String, RateLimiter> getAll() {
        return CACHE;
    }

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static RateLimiter getRateLimiterByKey(String hostId) {
        return CACHE.getIfPresent(hostId);
    }

    @Override
    public void start() {
        LogUtils.info(log, "CacheGroupRateLimiter start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    @Override
    public void close() {
        LogUtils.info(log, "CacheGroupRateLimiter stop");
    }
}
