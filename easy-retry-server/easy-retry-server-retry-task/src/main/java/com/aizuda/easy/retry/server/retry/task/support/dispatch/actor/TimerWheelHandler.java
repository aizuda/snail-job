package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-22 17:03
 */
@Component
@Slf4j
public class TimerWheelHandler implements Lifecycle {

    private static HashedWheelTimer timer = null;

    private static Cache<String, Timeout> cache;

    @Override
    public void start() {

        // TODO 支持可配置
        // tickDuration 和 timeUnit 一格的时间长度
        // ticksPerWheel 一圈有多少格
        timer = new HashedWheelTimer(
                new CustomizableThreadFactory("retry-task-timer-wheel-"), 100,
                TimeUnit.MILLISECONDS, 1024);

        timer.start();

        cache = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    public static void register(String groupName, String uniqueId, TimerTask task, long delay, TimeUnit unit) {

        if (delay < 0) {
            delay = 0;
        }

        // TODO 支持可配置
        if (delay > 60 * 1000) {
            LogUtils.warn(log, "距离下次执行时间过久, 不满足进入时间轮的条件. groupName:[{}] uniqueId:[{}] delay:[{}ms]",
                    groupName, uniqueId,  delay);
            return;
        }

        Timeout timeout = getTimeout(groupName, uniqueId);
        if (Objects.isNull(timeout)) {
            try {
                timeout = timer.newTimeout(task, delay, unit);
                cache.put(getKey(groupName, uniqueId), timeout);
            } catch (Exception e) {
                LogUtils.error(log, "加入时间轮失败. groupName:[{}] uniqueId:[{}]",
                        groupName, uniqueId, e);
            }
        }

    }

    private static String getKey(String groupName, String uniqueId) {
        return groupName.concat(StrUtil.UNDERLINE).concat(uniqueId);
    }

    public static Timeout getTimeout(String groupName, String uniqueId) {
        return cache.getIfPresent(getKey(groupName, uniqueId));
    }

    public static boolean isExisted(String groupName, String uniqueId) {
        return Objects.nonNull(cache.getIfPresent(getKey(groupName, uniqueId)));
    }

    public static boolean cancel(String groupName, String uniqueId) {
        String key = getKey(groupName, uniqueId);
        Timeout timeout = cache.getIfPresent(key);
        if (Objects.isNull(timeout)) {
            return false;
        }

        cache.invalidate(key);
        return timeout.cancel();
    }

    public static void clearCache(String groupName, String uniqueId) {
        cache.invalidate(getKey(groupName, uniqueId));
    }

    @Override
    public void close() {
        timer.stop();
        cache.invalidateAll();
    }
}
