package com.aizuda.easy.retry.server.common.cache;

import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

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

    private static Cache<String /*groupName*/, String/*namespaceId*/> CACHE;

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static ConcurrentMap<String, String> getAllConsumerGroupName() {
        return CACHE.asMap();
    }

    /**
     * 无缓存时添加
     * 有缓存时更新
     *
     * @return 缓存对象
     */
    public static synchronized void addOrUpdate(String groupName, String namespaceId) {
        CACHE.put(groupName, namespaceId);
    }

    public static void remove(String groupName) {
        EasyRetryLog.LOCAL.debug("Remove consumer cache. groupName:[{}]", groupName);
        CACHE.invalidate(groupName);
    }

    public static void clear() {
        CACHE.invalidateAll();
    }

    @Override
    public void start() {
        EasyRetryLog.LOCAL.info("CacheRegisterTable start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                // 若当前节点不在消费次组，则自动到期删除
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void close() {
        EasyRetryLog.LOCAL.info("CacheRegisterTable stop");
        CACHE.invalidateAll();
    }
}
