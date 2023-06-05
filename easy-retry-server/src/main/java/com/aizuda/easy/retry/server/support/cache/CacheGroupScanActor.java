package com.aizuda.easy.retry.server.support.cache;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.server.enums.TaskTypeEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.aizuda.easy.retry.server.support.Lifecycle;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 缓存组扫描Actor
 *
 * @author www.byteblogs.com
 * @date 2021-10-30
 * @since 2.0
 */
@Component
@Data
@Slf4j
public class CacheGroupScanActor implements Lifecycle {

    private static Cache<String, ActorRef> CACHE;

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static ActorRef get(String groupName, TaskTypeEnum typeEnum) {
       return CACHE.getIfPresent(groupName.concat(typeEnum.name()));
    }

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static void put(String groupName, TaskTypeEnum typeEnum, ActorRef actorRef) {
         CACHE.put(groupName.concat(typeEnum.name()), actorRef);
    }

    @Override
    public void start() {
        LogUtils.info(log, "CacheGroupScanActor start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    @Override
    public void close() {
        LogUtils.info(log, "CacheGroupScanActor stop");
        CACHE.invalidateAll();
    }
}
