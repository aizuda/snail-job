package com.aizuda.snail.job.server.retry.task.support.timer;

import cn.hutool.core.lang.Pair;
import com.aizuda.snail.job.common.log.EasyRetryLog;
import com.aizuda.snail.job.server.common.Lifecycle;
import com.aizuda.snail.job.server.retry.task.support.idempotent.TimerIdempotent;
import io.netty.util.HashedWheelTimer;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: opensnail
 * @date : 2023-09-22 17:03
 */
@Component
@Slf4j
public class RetryTimerWheel implements Lifecycle {

    private static final int TICK_DURATION = 500;
    private static final String THREAD_NAME_PREFIX = "retry-task-timer-wheel-";
    private static HashedWheelTimer timer = null;
    private static final ThreadPoolExecutor executor =
            new ThreadPoolExecutor(16, 16, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), new CustomizableThreadFactory(THREAD_NAME_PREFIX));

    private static final TimerIdempotent idempotent = new TimerIdempotent();

    @Override
    public void start() {
        timer = new HashedWheelTimer(
                new CustomizableThreadFactory(THREAD_NAME_PREFIX), TICK_DURATION, TimeUnit.MILLISECONDS, 512,
                true, -1, executor);
        timer.start();
    }

    public static void register(Pair<String/*groupName*/, String/*namespaceId*/> pair, String uniqueId, TimerTask task, long delay, TimeUnit unit) {

        if (!isExisted(pair, uniqueId)) {
            delay = delay < 0 ? 0 : delay;
            try {
                timer.newTimeout(task, delay, unit);
                idempotent.set(pair, uniqueId);
            } catch (Exception e) {
                EasyRetryLog.LOCAL.error("加入时间轮失败. uniqueId:[{}]", uniqueId, e);
            }
        }
    }

    public static boolean isExisted(Pair<String/*groupName*/, String/*namespaceId*/> pair, String uniqueId) {
        return idempotent.isExist(pair, uniqueId);
    }

    public static void clearCache(Pair<String/*groupName*/, String/*namespaceId*/> pair, String uniqueId) {
        idempotent.clear(pair, uniqueId);
    }

    @Override
    public void close() {
        timer.stop();
    }
}
