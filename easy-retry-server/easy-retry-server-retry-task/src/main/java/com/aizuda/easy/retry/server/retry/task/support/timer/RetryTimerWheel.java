package com.aizuda.easy.retry.server.retry.task.support.timer;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.retry.task.support.idempotent.TimerIdempotent;
import io.netty.util.HashedWheelTimer;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-22 17:03
 */
@Component
@Slf4j
public class RetryTimerWheel implements Lifecycle {

    private static final int TICK_DURATION = 500;
    private static final String THREAD_NAME_PREFIX = "retry-task-timer-wheel-";
    private static HashedWheelTimer timer = null;

    private static final TimerIdempotent idempotent = new TimerIdempotent();

    @Override
    public void start() {
        timer = new HashedWheelTimer(
            new CustomizableThreadFactory(THREAD_NAME_PREFIX), TICK_DURATION, TimeUnit.MILLISECONDS);
        timer.start();
    }

    public static void register(String groupName, String uniqueId, TimerTask task, long delay, TimeUnit unit) {

        if (!isExisted(groupName, uniqueId)) {
            delay = delay < 0 ? 0 : delay;
            try {
                timer.newTimeout(task, delay, unit);
                idempotent.set(uniqueId, uniqueId);
            } catch (Exception e) {
                LogUtils.error(log, "加入时间轮失败. uniqueId:[{}]", uniqueId, e);
            }
        }
    }

    public static boolean isExisted(String groupName, String uniqueId) {
        if (StrUtil.isNotBlank(groupName) || StrUtil.isNotBlank(uniqueId)) {
            return Boolean.FALSE;
        }
        return idempotent.isExist(groupName, uniqueId);
    }

    public static void clearCache(String groupName, String uniqueId) {
        idempotent.clear(groupName, uniqueId);
    }

    @Override
    public void close() {
        timer.stop();
    }
}
