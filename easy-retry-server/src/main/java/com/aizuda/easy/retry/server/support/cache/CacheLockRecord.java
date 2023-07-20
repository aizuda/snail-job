package com.aizuda.easy.retry.server.support.cache;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.support.Lifecycle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * @author www.byteblogs.com
 * @date 2023-07-20 22:53:21
 * @since 2.1.0
 */
@Slf4j
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

    public static void clear() {
        CACHE.invalidateAll();
    }

    @Override
    public void start() {
        LogUtils.info(log, "CacheLockRecord start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    @Override
    public void close() {

    }
}
