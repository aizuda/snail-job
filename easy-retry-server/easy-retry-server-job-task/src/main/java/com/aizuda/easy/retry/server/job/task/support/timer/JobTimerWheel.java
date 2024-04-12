package com.aizuda.easy.retry.server.job.task.support.timer;

import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.job.task.support.idempotent.TimerIdempotent;
import io.netty.util.HashedWheelTimer;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: opensnail
 * @date : 2023-09-22 17:03
 * @since : 2.4.0
 */
@Component
@Slf4j
public class JobTimerWheel implements Lifecycle {

    private static final int TICK_DURATION = 100;
    private static final String THREAD_NAME_PREFIX = "job-task-timer-wheel-";
    private static HashedWheelTimer timer = null;
    private static final ThreadPoolExecutor executor =
            new ThreadPoolExecutor(32, 32, 10, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(), new CustomizableThreadFactory(THREAD_NAME_PREFIX));

    private static final TimerIdempotent idempotent = new TimerIdempotent();

    @Override
    public void start() {
        timer = new HashedWheelTimer(
                new CustomizableThreadFactory(THREAD_NAME_PREFIX), TICK_DURATION,
                TimeUnit.MILLISECONDS, 512, true, -1, executor);
        timer.start();
    }

    public static void register(Integer taskType, Long uniqueId, TimerTask task, long delay, TimeUnit unit) {

        if (!isExisted(taskType, uniqueId)) {
            log.debug("加入时间轮. delay:[{}ms] taskType:[{}] uniqueId:[{}]", delay, taskType, uniqueId);
            delay = delay < 0 ? 0 : delay;
            try {
                timer.newTimeout(task, delay, unit);
                idempotent.set(uniqueId, uniqueId);
            } catch (Exception e) {
                EasyRetryLog.LOCAL.error("加入时间轮失败. uniqueId:[{}]", uniqueId, e);
            }
        }
    }

    public static boolean isExisted(Integer taskType, Long uniqueId) {
        return idempotent.isExist(Long.valueOf(taskType), uniqueId);
    }

    public static void clearCache(Integer taskType, Long uniqueId) {
        idempotent.clear(Long.valueOf(taskType), uniqueId);
    }

    @Override
    public void close() {
        timer.stop();
    }
}
