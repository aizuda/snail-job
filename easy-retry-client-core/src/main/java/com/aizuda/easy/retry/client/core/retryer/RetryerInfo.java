package com.aizuda.easy.retry.client.core.retryer;

import com.aizuda.easy.retry.client.core.IdempotentIdGenerate;
import com.aizuda.easy.retry.client.core.strategy.RetryMethod;
import com.aizuda.easy.retry.client.core.callback.RetryCompleteCallback;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 15:06
 */
@Data
@AllArgsConstructor
public class RetryerInfo {

    private final String scene;
    private final String executorClassName;
    private final Set<Class<? extends Throwable>> include;
    private final Set<Class<? extends Throwable>> exclude;
    private final Object executor;
    private final Method executorMethod;
    private final RetryType retryType;
    private final Integer localTimes;
    private final Integer localInterval;
    private final Class<? extends IdempotentIdGenerate> idempotentIdGenerate;
    private final String bizNo;
    private final Class<? extends RetryMethod> retryMethod;
    private final boolean isThrowException;
    private final Class<? extends RetryCompleteCallback> retryCompleteCallback;
}
