package com.aizuda.snailjob.server.job.task.support.handler;

import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.lock.LockBuilder;
import com.aizuda.snailjob.server.common.lock.LockManager;
import com.aizuda.snailjob.server.common.lock.LockProvider;
import com.aizuda.snailjob.server.job.task.support.LockExecutor;
import com.github.rholder.retry.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
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
     * @param lockExecutor  执行器
     * @param lockName      锁名称
     * @param lockAtMost    锁超时时间
     * @param sleepTime     重试间隔
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
                .withWaitStrategy(WaitStrategies.randomWait(sleepTime.toMillis(), TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(maxRetryTimes))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(final Attempt<V> attempt) {
                        Object result = null;
                        if (attempt.hasResult()) {
                            try {
                                result = attempt.get();
                            } catch (ExecutionException ignored) {
                            }
                        }

                        SnailJobLog.LOCAL.debug("第【{}】次尝试获取锁. lockName:[{}] result:[{}] treadName:[{}]",
                                attempt.getAttemptNumber(), lockName, result, Thread.currentThread().getName());
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
                Attempt<?> lastFailedAttempt = re.getLastFailedAttempt();
                if (lastFailedAttempt.hasException()) {
                    throwable = lastFailedAttempt.getExceptionCause();
                }
            }

            SnailJobLog.LOCAL.warn("lock execute error. lockName:[{}]", lockName, throwable);
        } finally {
            if (lock) {
                lockProvider.unlock();
                SnailJobLog.LOCAL.debug("[{}] 锁已释放", lockName);
            } else {
                // 未获取到锁直接清除线程中存储的锁信息
                LockManager.clear();
            }
        }

    }

    /**
     * 获取分布式锁
     *
     * @param lockExecutor 执行器
     * @param lockName     锁名称
     * @param lockAtMost   锁超时时间
     */
    public void lockWithDisposable(LockExecutor lockExecutor, String lockName, Duration lockAtMost) {

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
            SnailJobLog.LOCAL.error("lock execute error. lockName:[{}]", lockName, e);
        } finally {
            if (lock) {
                lockProvider.unlock();
                SnailJobLog.LOCAL.debug("[{}] 锁已释放", lockName);
            } else {
                // 未获取到锁直接清除线程中存储的锁信息
                LockManager.clear();
            }
        }
    }

}
