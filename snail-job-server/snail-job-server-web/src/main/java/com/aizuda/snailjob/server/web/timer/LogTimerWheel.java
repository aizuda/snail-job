package com.aizuda.snailjob.server.web.timer;

import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.TimerTask;
import com.aizuda.snailjob.server.common.idempotent.TimerIdempotent;
import io.netty.util.HashedWheelTimer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.server.job.task.support.timer
 * @Project：snail-job
 * @Date：2025/3/24 14:00
 * @Filename：LogTimerWheel
 * @since 1.5.0
 */
public class LogTimerWheel {
    private static final int TICK_DURATION = 100;
    private static final String THREAD_NAME_PREFIX = "log-timer-wheel-";
    private static HashedWheelTimer timer = null;
    private static final ThreadPoolExecutor executor =
            new ThreadPoolExecutor(16, 16, 10, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(), new CustomizableThreadFactory(THREAD_NAME_PREFIX));

    private static final TimerIdempotent idempotent = new TimerIdempotent();

    static {
        timer = new HashedWheelTimer(
                new CustomizableThreadFactory(THREAD_NAME_PREFIX), TICK_DURATION,
                TimeUnit.MILLISECONDS, 512, true, -1, executor);
        timer.start();
    }

    /**
     * 定时任务批次日志添加时间轮
     *
     * @param task  任务
     * @param delay 延迟时间
     */
    public static synchronized void registerWithJobTaskLog(Supplier<TimerTask<String>> task, Duration delay) {
        TimerTask<String> timerTask = task.get();
        register(timerTask.idempotentKey(), timerTask, delay);
    }

    public static synchronized void register(String idempotentKey, TimerTask<String> task, Duration delay) {

        register(idempotentKey, hashedWheelTimer -> {
            SnailJobLog.LOCAL.debug("加入时间轮. delay:[{}ms] idempotentKey:[{}]", delay, idempotentKey);
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
