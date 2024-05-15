package com.aizuda.snailjob.server.common.cache;

import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 缓存通知限流组件
 *
 * @author zuoJunLin
 * @date 2023-11-20
 * @since 2.5.0
 */
@Component
@Data
@Slf4j
public class CacheNotifyRateLimiter implements Lifecycle {

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
    public static RateLimiter getRateLimiterByKey(String key) {
        return CACHE.getIfPresent(key);
    }

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static void put(String key, RateLimiter rateLimiter) {
        CACHE.put(key, rateLimiter);
    }

    @Override
    public void start() {
        SnailJobLog.LOCAL.info("CacheNotifyRateLimiter start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public void close() {
        SnailJobLog.LOCAL.info("CacheNotifyRateLimiter stop");
    }
}
