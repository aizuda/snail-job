package com.aizuda.easy.retry.server.support.cache;

import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.support.Lifecycle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;

/**
 * 组注册表
 *
 * @author www.byteblogs.com
 * @date 2021-10-30
 * @since 1.6.0
 */
@Component
@Slf4j
public class CacheGroup implements Lifecycle {

    private static Cache<String/*groupName*/, String/*groupName*/> CACHE;

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static Set<String> getAllGroup() {
        ConcurrentMap<String, String> concurrentMap = CACHE.asMap();
        if (CollectionUtils.isEmpty(concurrentMap)) {
            return Collections.EMPTY_SET;
        }

        return new TreeSet<>(concurrentMap.values());

    }

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static String get(String hostId) {
        return CACHE.getIfPresent(hostId);
    }

    /**
     * 无缓存时添加
     * 有缓存时更新
     *
     * @return 缓存对象
     */
    public static synchronized void addOrUpdate(String groupName) {
        CACHE.put(groupName, groupName);
    }

    public static void remove(String groupName) {
        CACHE.invalidate(groupName);
    }

    @Override
    public void start() {
        LogUtils.info(log, "CacheGroup start");
        CACHE = CacheBuilder.newBuilder()
            // 设置并发级别为cpu核心数
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .build();
    }

    @Override
    public void close() {
        LogUtils.info(log, "CacheGroup stop");
        CACHE.invalidateAll();
    }
}
