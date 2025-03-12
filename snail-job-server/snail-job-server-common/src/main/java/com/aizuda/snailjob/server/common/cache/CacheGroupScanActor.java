package com.aizuda.snailjob.server.common.cache;

import  org.apache.pekko.actor.ActorRef;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 缓存组扫描Actor
 *
 * @author opensnail
 * @date 2021-10-30
 * @since 1.0.0
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
    public static ActorRef get(String groupName, SyetemTaskTypeEnum typeEnum) {
        return CACHE.getIfPresent(groupName.concat(typeEnum.name()));
    }

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static void put(String groupName, SyetemTaskTypeEnum typeEnum, ActorRef actorRef) {
        CACHE.put(groupName.concat(typeEnum.name()), actorRef);
    }

    @Override
    public void start() {
        SnailJobLog.LOCAL.info("CacheGroupScanActor start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    @Override
    public void close() {
        SnailJobLog.LOCAL.info("CacheGroupScanActor stop");
        CACHE.invalidateAll();
    }
}
