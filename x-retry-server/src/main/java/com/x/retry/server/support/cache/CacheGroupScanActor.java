package com.x.retry.server.support.cache;

import akka.actor.ActorRef;
import com.x.retry.common.core.log.LogUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.x.retry.server.support.Lifecycle;
import lombok.Data;
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
public class CacheGroupScanActor implements Lifecycle {

    private static Cache<String, ActorRef> CACHE;

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static Cache<String, ActorRef> getAll() {
        return CACHE;
    }

    @Override
    public void start() {
        LogUtils.info("CacheGroupScanActor start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    @Override
    public void close() {
        LogUtils.info("CacheGroupScanActor stop");
    }
}
