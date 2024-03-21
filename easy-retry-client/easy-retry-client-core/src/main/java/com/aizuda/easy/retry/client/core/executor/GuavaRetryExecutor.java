package com.aizuda.easy.retry.client.core.executor;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.client.core.RetryExecutorParameter;
import com.aizuda.easy.retry.client.core.cache.RetryerInfoCache;
import com.aizuda.easy.retry.client.core.exception.EasyRetryClientException;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategy;
import com.github.rholder.retry.WaitStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Guava 重试执行器
 *
 * @author: www.byteblogs.com
 * @date : 2022-03-03 18:07
 * @since 1.3.0
 */
@Slf4j
public class GuavaRetryExecutor extends AbstractRetryExecutor<WaitStrategy, StopStrategy> {

    public GuavaRetryExecutor(String sceneName, String executorClassName) {
        retryerInfo = RetryerInfoCache.get(sceneName, executorClassName);
        Assert.notNull(retryerInfo, () -> new EasyRetryClientException("retryerInfo is null sceneName:[{}] executorClassName:[{}]", sceneName, executorClassName));
    }

    public GuavaRetryExecutor() {
    }

    @Override
    public Retryer build(RetryExecutorParameter<WaitStrategy, StopStrategy> parameter) {

        RetryerBuilder<Object> retryerBuilder = RetryerBuilder.newBuilder();
        retryerBuilder.retryIfException(throwable -> true);
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
            EasyRetryLog.LOCAL.debug("业务系统重试异常：",e.getLastFailedAttempt().getExceptionCause());
            retryError.accept(e.getLastFailedAttempt().getExceptionCause());
        }

        return result;
    }
}
