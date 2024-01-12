package com.aizuda.easy.retry.server.common.cache;

import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.lock.LockManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 缓存本地的分布式锁的名称
 *
 * @author www.byteblogs.com
 * @date 2023-07-20 22:53:21
 * @since 2.1.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CacheLockRecord implements Lifecycle {
    private static Cache<String, String> CACHE;

    public static void addLockRecord(String lockName) {
        CACHE.put(lockName, lockName);
    }

    public static boolean lockRecordRecentlyCreated(String lockName) {
        return CACHE.asMap().containsKey(lockName);
    }

    public static long getSize() {
        return CACHE.size();
    }

    public static void remove(String lockName) {
        CACHE.invalidate(lockName);
        LockManager.clear();
    }

    public static void clear() {
        CACHE.invalidateAll();
    }

    @Override
    public void start() {
        EasyRetryLog.LOCAL.info("CacheLockRecord start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .expireAfterWrite(Duration.ofHours(1))
                .build();
    }

    @Override
    public void close() {
        CACHE.invalidateAll();
    }
}
