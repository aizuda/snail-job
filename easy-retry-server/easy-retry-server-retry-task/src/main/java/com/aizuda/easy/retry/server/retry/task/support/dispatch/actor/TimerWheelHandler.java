package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor;

import com.aizuda.easy.retry.server.common.Lifecycle;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-22 17:03
 */
@Component
public class TimerWheelHandler implements Lifecycle {

    private static HashedWheelTimer hashedWheelTimer = null;

   public static ConcurrentHashMap<String, Timeout> taskConcurrentHashMap = new ConcurrentHashMap<>();

    @Override
    public void start() {
        hashedWheelTimer = new HashedWheelTimer(
            new CustomizableThreadFactory("retry-task-timer-wheel"), 10, TimeUnit.MILLISECONDS, 16);
        hashedWheelTimer.start();
    }

    public static void register(String groupName, String uniqueId, TimerTask task, long delay, TimeUnit unit) {
        Timeout timeout = hashedWheelTimer.newTimeout(task, delay, unit);
        taskConcurrentHashMap.put(groupName.concat("_").concat(uniqueId), timeout);
    }

    public static boolean cancel(Long taskId) {
        Timeout timeout = taskConcurrentHashMap.get(taskId);
        return timeout.cancel();
    }

    @Override
    public void close() {
        hashedWheelTimer.stop();
    }
}
