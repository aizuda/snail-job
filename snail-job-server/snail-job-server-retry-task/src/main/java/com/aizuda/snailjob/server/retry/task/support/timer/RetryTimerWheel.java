package com.aizuda.snailjob.server.retry.task.support.timer;

import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.retry.task.support.idempotent.IdempotentHolder;
import com.aizuda.snailjob.server.retry.task.support.idempotent.TimerIdempotent;
import io.netty.util.HashedWheelTimer;
import io.netty.util.TimerTask;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: opensnail
 * @date : 2023-09-22 17:03
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RetryTimerWheel {

    private static final int TICK_DURATION = 500;
    private static final String THREAD_NAME_PREFIX = "retry-task-timer-wheel-";
    private static HashedWheelTimer timer = null;
    private static final ThreadPoolExecutor executor =
            new ThreadPoolExecutor(16, 16, 10, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(), new CustomizableThreadFactory(THREAD_NAME_PREFIX));

    private static final TimerIdempotent idempotent = IdempotentHolder.getTimerIdempotent();

    static {
        timer = new HashedWheelTimer(
            new CustomizableThreadFactory(THREAD_NAME_PREFIX), TICK_DURATION, TimeUnit.MILLISECONDS, 512,
            true, -1, executor);
        timer.start();
    }

    public static void register(String idempotentKey, TimerTask task, Duration delay) {

        if (!isExisted(idempotentKey)) {
            try {
                timer.newTimeout(task, Math.max(delay.toMillis(), 0), TimeUnit.MILLISECONDS);
                idempotent.set(idempotentKey);
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("加入时间轮失败. uniqueId:[{}]", idempotentKey, e);
            }
        }
    }

    public static boolean isExisted(String idempotentKey) {
        return idempotent.isExist(idempotentKey);
    }

    public static void clearCache(String idempotentKey) {
        idempotent.clear(idempotentKey);
    }

}
