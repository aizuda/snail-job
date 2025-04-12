package com.aizuda.snailjob.server.job.task.support.timer;

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
 * @author: opensnail
 * @date : 2023-09-22 17:03
 * @since : 2.4.0
 */
public class JobTimerWheel {

    private static final int TICK_DURATION = 100;
    private static final String THREAD_NAME_PREFIX = "job-task-timer-wheel-";
    private static HashedWheelTimer timer = null;
    private static final ThreadPoolExecutor executor =
            new ThreadPoolExecutor(32, 32, 10, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(), new CustomizableThreadFactory(THREAD_NAME_PREFIX));

    private static final TimerIdempotent idempotent = new TimerIdempotent();

    static {
        timer = new HashedWheelTimer(
                new CustomizableThreadFactory(THREAD_NAME_PREFIX), TICK_DURATION,
                TimeUnit.MILLISECONDS, 512, true, -1, executor);
        timer.start();
    }

    /**
     * 定时任务添加时间轮
     *
     * @param task  任务
     * @param delay 延迟时间
     */
    public static synchronized void registerWithWorkflow(Supplier<TimerTask<String>> task, Duration delay) {
        TimerTask<String> timerTask = task.get();
        register(timerTask.idempotentKey(), timerTask, delay);
    }

    /**
     * 工作流任务添加时间轮
     * 虽然job和Workflow 添加时间轮方法逻辑一样为了后面做一些不同的逻辑，这里兼容分开写
     *
     * @param task  任务
     * @param delay 延迟时间
     */
    public static synchronized void registerWithJob(Supplier<TimerTask<String>> task, Duration delay) {
        TimerTask<String> timerTask = task.get();
        register(timerTask.idempotentKey(), timerTask, delay);
    }

    public static synchronized void register(String idempotentKey, TimerTask<String> task, Duration delay) {

        register(idempotentKey, hashedWheelTimer -> {
            SnailJobLog.LOCAL.debug("Joining time wheel. delay:[{}ms] idempotentKey:[{}]", delay, idempotentKey);
            timer.newTimeout(task, Math.max(delay.toMillis(), 0), TimeUnit.MILLISECONDS);
        });
    }

    public static synchronized void register(String idempotentKey, Consumer<HashedWheelTimer> consumer) {

        if (!isExisted(idempotentKey)) {
            try {
                consumer.accept(timer);
                idempotent.set(idempotentKey);
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("Failed to join time wheel. uniqueId:[{}]", idempotentKey, e);
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
