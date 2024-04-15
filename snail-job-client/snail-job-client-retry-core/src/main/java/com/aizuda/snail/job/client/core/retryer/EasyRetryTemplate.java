package com.aizuda.snail.job.client.core.retryer;

import com.aizuda.snail.job.client.core.RetryOperations;
import com.aizuda.snail.job.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.snail.job.client.core.strategy.ExecutorMethod;
import com.aizuda.snail.job.client.core.strategy.RetryStrategy;

import java.util.Objects;

/**
 * 手动生成重试任务模板类
 *
 * @author: opensnail
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

        Integer stage = RetrySiteSnapshot.getStage();
        try {
            retryStrategy.openRetry(scene, executorMethodClass.getName(), params);
        } finally {
            // stage == null 则非嵌套重试, 需求清除线程记录的数据信息
            // stage != null 则由上层调度的进行清除线程记录的数据信息
            if (Objects.isNull(stage)) {
                RetrySiteSnapshot.removeAll();
            } else {
                // 还原原始的重试阶段
                RetrySiteSnapshot.setStage(stage);
            }
        }
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
