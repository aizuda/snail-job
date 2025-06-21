package com.aizuda.snailjob.client.core.executor;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.client.core.RetryCondition;
import com.aizuda.snailjob.client.core.RetryExecutorParameter;
import com.aizuda.snailjob.client.core.cache.RetryerInfoCache;
import com.aizuda.snailjob.client.core.exception.RetryIfResultException;
import com.aizuda.snailjob.client.core.exception.SnailRetryClientException;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.github.rholder.retry.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Guava 重试执行器
 *
 * @author: opensnail
 * @date : 2022-03-03 18:07
 * @since 1.3.0
 */
@Slf4j
public class GuavaRetryExecutor extends AbstractRetryExecutor<WaitStrategy, StopStrategy> {

    public GuavaRetryExecutor(String sceneName, String executorClassName) {
        retryerInfo = RetryerInfoCache.get(sceneName, executorClassName);
        Assert.notNull(retryerInfo, () -> new SnailRetryClientException("retryerInfo is null sceneName:[{}] executorClassName:[{}]", sceneName, executorClassName));
    }

    public GuavaRetryExecutor() {
    }

    @Override
    public Retryer build(RetryExecutorParameter<WaitStrategy, StopStrategy> parameter) {
        RetryerBuilder<Object> retryerBuilder = RetryerBuilder.newBuilder();
        retryerBuilder.retryIfException(throwable -> true);

        // 必须是需要根据结果判断是否重试才设置retryIfResult
        if (Objects.nonNull(retryerInfo) && !retryerInfo.getRetryCondition().isAssignableFrom(RetryCondition.NoRetry.class)) {
            retryerBuilder.retryIfResult(this::retryIf);
        }

        retryerBuilder.withWaitStrategy(parameter.backOff());
        retryerBuilder.withStopStrategy(parameter.stop());
        for (RetryListener retryListener : parameter.getRetryListeners()) {
            retryerBuilder.withRetryListener(retryListener);
        }

        return retryerBuilder.build();
    }

    @Override
    public <V> V call(Retryer<V> retryer, Callable<V> callable, Consumer<Throwable> retryError, Consumer<V> retrySuccess) throws Exception {

        V result = null;
        try {
            result = retryer.call(callable);
            retrySuccess.accept(result);
        } catch (RetryException e) {
            // 重试完成，仍然失败
            Attempt<?> attempt = e.getLastFailedAttempt();
            if (attempt.hasException()) {
                retryError.accept(e.getLastFailedAttempt().getExceptionCause());
                SnailJobLog.LOCAL.debug("Business system retry exception:", e.getLastFailedAttempt().getExceptionCause());
            } else {
                SnailJobLog.LOCAL.debug("Business system retry exception:", e.getLastFailedAttempt().getResult());
                // 这里必须设置一个异常，本地重试才能上报
                retryError.accept(new RetryIfResultException("Business system retry exception. result: {}", e.getLastFailedAttempt().getResult()));
            }

        }

        return result;
    }

    private boolean retryIf(Object result) {
        try {
            Class<? extends RetryCondition> retryConditionClass = retryerInfo.getRetryCondition();
            RetryCondition retryCondition = retryConditionClass.getDeclaredConstructor().newInstance();
            return retryCondition.shouldRetry(result);
        } catch (Throwable e) {
            SnailJobLog.LOCAL.error("Retry condition fail. scene:[{}] executorClassName:[{}]", retryerInfo.getScene(),
                    retryerInfo.getExecutorClassName(), e);
        }

        return false;
    }

}
