package com.aizuda.snailjob.server.job.task.support.timer;

import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.job.task.support.idempotent.TimerIdempotent;
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

//    @Deprecated
//    public static synchronized void register(Integer taskType, Long uniqueId, TimerTask<Long> task, long delay, TimeUnit unit) {
//
//        if (!isExisted(taskType, uniqueId)) {
//            SnailJobLog.LOCAL.debug("加入时间轮. delay:[{}ms] taskType:[{}] uniqueId:[{}]", delay, taskType, uniqueId);
//            delay = delay < 0 ? 0 : delay;
//            try {
//                timer.newTimeout(task, delay, unit);
//                idempotent.set(taskType, uniqueId);
//            } catch (Exception e) {
//                SnailJobLog.LOCAL.error("加入时间轮失败. uniqueId:[{}]", uniqueId, e);
//            }
//        }
//    }

    public static synchronized void registerWithWorkflow(Supplier<TimerTask<Long>> task, Duration delay) {
        TimerTask<Long> timerTask = task.get();
        register(SyetemTaskTypeEnum.WORKFLOW.getType(), timerTask.getUniqueId(), timerTask, delay);
    }

    public static synchronized void registerWithJob(Supplier<TimerTask<Long>> task, Duration delay) {
        TimerTask<Long> timerTask = task.get();
        register(SyetemTaskTypeEnum.JOB.getType(), timerTask.getUniqueId(), timerTask, delay);
    }

    public static synchronized void register(Integer taskType, Long uniqueId, TimerTask<Long> task, Duration delay) {

        register(taskType, uniqueId, new Consumer<HashedWheelTimer>() {
            @Override
            public void accept(final HashedWheelTimer hashedWheelTimer) {
                SnailJobLog.LOCAL.debug("加入时间轮. delay:[{}ms] taskType:[{}] uniqueId:[{}]", delay, taskType, uniqueId);
                timer.newTimeout(task, Math.max(delay.toMillis(), 0), TimeUnit.MILLISECONDS);
            }
        });
    }

    public static synchronized void register(Integer taskType, Long uniqueId, Consumer<HashedWheelTimer> consumer) {

        if (!isExisted(taskType, uniqueId)) {
            try {
                consumer.accept(timer);
                idempotent.set(taskType, uniqueId);
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("加入时间轮失败. uniqueId:[{}]", uniqueId, e);
            }
        }
    }

    public static boolean isExisted(Integer taskType, Long uniqueId) {
        return idempotent.isExist(taskType, uniqueId);
    }

    public static void clearCache(Integer taskType, Long uniqueId) {
        idempotent.clear(taskType, uniqueId);
    }

}
