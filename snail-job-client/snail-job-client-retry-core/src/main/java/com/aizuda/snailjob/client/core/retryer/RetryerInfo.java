package com.aizuda.snailjob.client.core.retryer;

import com.aizuda.snailjob.client.core.IdempotentIdGenerate;
import com.aizuda.snailjob.client.core.strategy.ExecutorMethod;
import com.aizuda.snailjob.client.core.callback.RetryCompleteCallback;
import com.aizuda.snailjob.client.core.strategy.ExecutorMethod;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 定义重试场景的信息
 *
 * @author: opensnail
 * @date : 2022-03-03 15:06
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public class RetryerInfo {

    private final String scene;
    private final String executorClassName;
    private final Set<Class<? extends Throwable>> include;
    private final Set<Class<? extends Throwable>> exclude;
    private final Object executor;
    private final Method method;
    private final RetryType retryType;
    private final Integer localTimes;
    private final Integer localInterval;
    private final Class<? extends IdempotentIdGenerate> idempotentIdGenerate;
    private final String bizNo;
    private final Class<? extends ExecutorMethod> executorMethod;
    private final boolean isThrowException;
    private final Class<? extends RetryCompleteCallback> retryCompleteCallback;
    private final boolean async;
    private final boolean forceReport;
    private final long timeout;
    private final TimeUnit unit;


}
