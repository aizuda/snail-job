package com.aizuda.snailjob.server.retry.task.support.idempotent;

/**
 * @author: opensnail
 * @date : 2024-05-23
 * @since : sj_1.0.0
 */
public class IdempotentHolder {

    private IdempotentHolder() {
    }

    public static RetryIdempotentStrategyHandler getRetryIdempotent() {
        return SingletonHolder.RETRY_IDEMPOTENT_INSTANCE;
    }

    public static TimerIdempotent getTimerIdempotent() {
        return SingletonHolder.TIMER_IDEMPOTENT;
    }

    private static class SingletonHolder {
        private static final RetryIdempotentStrategyHandler RETRY_IDEMPOTENT_INSTANCE = new RetryIdempotentStrategyHandler();
        private static final TimerIdempotent TIMER_IDEMPOTENT = new TimerIdempotent();
    }
}
