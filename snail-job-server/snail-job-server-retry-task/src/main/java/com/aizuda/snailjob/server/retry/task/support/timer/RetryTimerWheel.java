package com.aizuda.snailjob.server.retry.task.support.timer;

import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.TimerTask;
import com.aizuda.snailjob.server.retry.task.support.idempotent.IdempotentHolder;
import com.aizuda.snailjob.server.retry.task.support.idempotent.TimerIdempotent;
import io.netty.util.HashedWheelTimer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    /**
     * 工作流任务添加时间轮
     * 虽然job和Workflow 添加时间轮方法逻辑一样为了后面做一些不同的逻辑，这里兼容分开写
     *
     * @param task  任务
     * @param delay 延迟时间
     */
    public static synchronized void registerWithRetry(Supplier<TimerTask<String>> task, Duration delay) {
        TimerTask<String> timerTask = task.get();
        register(timerTask.idempotentKey(), timerTask, delay);
    }

    public static synchronized void register(String idempotentKey, TimerTask<String> task, Duration delay) {

        register(idempotentKey, hashedWheelTimer -> {
            SnailJobLog.LOCAL.info("加入时间轮. delay:[{}ms] taskType:[{}]", delay, idempotentKey);
            timer.newTimeout(task, Math.max(delay.toMillis(), 0), TimeUnit.MILLISECONDS);
        });
    }

    public static synchronized void register(String idempotentKey, Consumer<HashedWheelTimer> consumer) {

        if (!isExisted(idempotentKey)) {
            try {
                consumer.accept(timer);
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
