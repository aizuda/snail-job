package com.x.retry.client.core.executor;

import com.github.rholder.retry.*;
import com.x.retry.client.core.RetryExecutorParameter;
import com.x.retry.client.core.cache.RetryerInfoCache;
import com.x.retry.common.core.log.LogUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 18:07
 */
@Slf4j
public class GuavaRetryExecutor extends AbstractRetryExecutor<WaitStrategy, StopStrategy> {

    public GuavaRetryExecutor(String sceneName, String executorClassName) {
        retryerInfo = RetryerInfoCache.get(sceneName, executorClassName);
    }

    public GuavaRetryExecutor() {
    }

    @Override
    public Retryer build(RetryExecutorParameter<WaitStrategy, StopStrategy> parameter) {

        RetryerBuilder<Object> retryerBuilder = RetryerBuilder.newBuilder();
        retryerBuilder.retryIfException(throwable -> parameter.exceptionPredicate().apply(throwable));
        retryerBuilder.withWaitStrategy(parameter.backOff());
        retryerBuilder.withStopStrategy(parameter.stop());
        for (RetryListener retryListener : parameter.getRetryListeners()) {
            retryerBuilder.withRetryListener(retryListener);
        }

        return retryerBuilder.build();
    }

    @Override
    public  <V> V call(Retryer<V> retryer, Callable<V> callable, Consumer<Throwable> retryError, Consumer<V> retrySuccess) throws Exception {

        V result = null;
        try {
            result = retryer.call(callable);
            retrySuccess.accept(result);
        } catch (RetryException e){
            // 重试完成，仍然失败
            LogUtils.error(log, "业务系统重试异常：",e.getLastFailedAttempt().getExceptionCause());
            retryError.accept(e.getLastFailedAttempt().getExceptionCause());
        }

        return result;
    }
}
