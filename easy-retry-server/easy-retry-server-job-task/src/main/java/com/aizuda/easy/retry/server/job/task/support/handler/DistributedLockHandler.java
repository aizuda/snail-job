package com.aizuda.easy.retry.server.job.task.support.handler;

import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.lock.LockBuilder;
import com.aizuda.easy.retry.server.common.lock.LockProvider;
import com.aizuda.easy.retry.server.job.task.support.LockExecutor;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author: xiaowoniu
 * @date : 2024-01-02
 * @since : 2.6.0
 */
@Component
@Slf4j
public class DistributedLockHandler {

    public void lockWithDisposableAndRetry(LockExecutor lockExecutor,
                                           String lockName, Duration lockAtMost,
                                           Duration sleepTime, Integer maxRetryTimes) {
        LockProvider lockProvider = LockBuilder.newBuilder()
                .withDisposable(lockName)
                .build();

        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .retryIfResult(result -> false)
                .withWaitStrategy(WaitStrategies.fixedWait(sleepTime.toMillis(), TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(maxRetryTimes))
                .build();

        boolean lock = false;
        try {
            lock = retryer.call(() -> lockProvider.lock(lockAtMost));
            if (lock) {
                lockExecutor.execute();
            }
        } catch (Exception e) {
            EasyRetryLog.LOCAL.error("lock execute error. lockName:[{}]", lockName, e);
        } finally {
            if (lock) {
                lockProvider.unlock();
            }
        }

    }

    /**
     * TODO 超时处理、自旋处理
     *
     * @param lockName
     * @param lockAtMost
     * @param lockExecutor
     */
    public void lockWithDisposable(String lockName, Duration lockAtMost, LockExecutor lockExecutor) {

        LockProvider lockProvider = LockBuilder.newBuilder()
                .withDisposable(lockName)
                .build();

        boolean lock = false;
        try {
            lock = lockProvider.lock(lockAtMost);
            if (lock) {
                lockExecutor.execute();
            }
        } catch (Exception e) {
            EasyRetryLog.LOCAL.error("lock execute error. lockName:[{}]", lockName, e);
        } finally {
            if (lock) {
                lockProvider.unlock();
            }
        }
    }

}
