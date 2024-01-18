package com.aizuda.easy.retry.server.job.task.support.handler;

import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.lock.LockBuilder;
import com.aizuda.easy.retry.server.common.lock.LockProvider;
import com.aizuda.easy.retry.server.job.task.support.LockExecutor;
import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类
 *
 * @author: xiaowoniu
 * @date : 2024-01-02
 * @since : 2.6.0
 */
@Component
@Slf4j
public class DistributedLockHandler {

    /**
     * 获取分布式锁并支持重试
     *
     * @param lockExecutor 执行器
     * @param lockName 锁名称
     * @param lockAtMost 锁超时时间
     * @param sleepTime 重试间隔
     * @param maxRetryTimes 重试次数
     */
    public void lockWithDisposableAndRetry(LockExecutor lockExecutor,
        String lockName, Duration lockAtMost,
        Duration sleepTime, Integer maxRetryTimes) {
        LockProvider lockProvider = LockBuilder.newBuilder()
            .withDisposable(lockName)
            .build();

        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
            .retryIfResult(result -> result.equals(Boolean.FALSE))
            .retryIfException(ex -> true)
            .withWaitStrategy(WaitStrategies.fixedWait(sleepTime.toMillis(), TimeUnit.MILLISECONDS))
            .withStopStrategy(StopStrategies.stopAfterAttempt(maxRetryTimes))
            .withRetryListener(new RetryListener() {
                @Override
                public <V> void onRetry(final Attempt<V> attempt) {
                    if (!attempt.hasResult()) {
                        EasyRetryLog.LOCAL.warn("第【{}】次尝试获取锁. lockName:[{}]",
                            attempt.getAttemptNumber(), lockName);
                    }
                }
            }).build();

        boolean lock = false;
        try {
            lock = retryer.call(() -> lockProvider.lock(lockAtMost));
            if (lock) {
                lockExecutor.execute();
            }
        } catch (Exception e) {
            Throwable throwable = e;
            if (e.getClass().isAssignableFrom(RetryException.class)) {
                RetryException re = (RetryException) e;
                throwable = re.getLastFailedAttempt().getExceptionCause();
            }

            EasyRetryLog.LOCAL.error("lock execute error. lockName:[{}]", lockName, throwable);
        } finally {
            if (lock) {
                lockProvider.unlock();
            }
        }

    }

    /**
     * 获取分布式锁
     *
     * @param lockExecutor 执行器
     * @param lockName 锁名称
     * @param lockAtMost 锁超时时间
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
