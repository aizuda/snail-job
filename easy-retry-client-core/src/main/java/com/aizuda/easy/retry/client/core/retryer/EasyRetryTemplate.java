package com.aizuda.easy.retry.client.core.retryer;

import com.aizuda.easy.retry.client.core.RetryOperations;
import com.aizuda.easy.retry.client.core.strategy.ExecutorMethod;
import com.aizuda.easy.retry.client.core.strategy.RetryStrategy;

/**
 * 手动生成重试任务模板类
 *
 * @author: www.byteblogs.com
 * @date : 2023-05-09 16:30
 * @since 1.3.0
 */
public class EasyRetryTemplate implements RetryOperations {

    private Class<? extends ExecutorMethod> executorMethodClass;
    private String scene;
    private Object[] params;
    private RetryStrategy retryStrategy;

    @Override
    public void executeRetry() {
        retryStrategy.openRetry(scene, executorMethodClass.getName(), params);
    }

    protected void setExecutorMethodClass(
        final Class<? extends ExecutorMethod> executorMethodClass) {
        this.executorMethodClass = executorMethodClass;
    }

    protected void setScene(final String scene) {
        this.scene = scene;
    }

    protected void setParams(final Object[] params) {
        this.params = params;
    }

    protected void setRetryStrategy(final RetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
    }
}
