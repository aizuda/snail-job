package com.aizuda.easy.retry.server.support.cache;

import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.support.Lifecycle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 当前POD负责消费的组
 *
 * @author www.byteblogs.com
 * @date 2021-10-30
 * @since 1.6.0
 */
@Component
@Slf4j
public class CacheConsumerGroup implements Lifecycle {

    private static Cache<String /*groupName*/, String/*groupName*/> CACHE;

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static Set<String> getAllConsumerGroupName() {
        ConcurrentMap<String, String> concurrentMap = CACHE.asMap();
        return new HashSet<>(concurrentMap.values());

    }

    /**
     * 无缓存时添加
     * 有缓存时更新
     *
     * @return 缓存对象
     */
    public static synchronized void addOrUpdate(String groupName) {
        LogUtils.info(log, "add consumer cache. groupName:[{}]", groupName);
        CACHE.put(groupName, groupName);
    }

    public static void remove(String groupName) {
        LogUtils.info(log, "Remove consumer cache. groupName:[{}]", groupName);
        CACHE.invalidate(groupName);
    }

    public static void clear() {
        CACHE.invalidateAll();
    }

    @Override
    public void start() {
        LogUtils.info(log, "CacheRegisterTable start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    @Override
    public void close() {
        LogUtils.info(log, "CacheRegisterTable stop");
        CACHE.invalidateAll();
    }
}
