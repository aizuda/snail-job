package com.aizuda.snailjob.client.core.retryer;

import com.aizuda.snailjob.client.core.strategy.ExecutorMethod;
import com.aizuda.snailjob.client.core.strategy.ManualRetryStrategies;
import com.aizuda.snailjob.client.core.strategy.RetryStrategy;
import com.aizuda.snailjob.common.core.context.SpringContext;

/**
 * 构建重试模板对象
 *
 * @author: opensnail
 * @date : 2023-05-10 10:17
 */
public class RetryTaskTemplateBuilder {

    private Class<? extends ExecutorMethod> executorMethodClass;
    private String scene;
    private Object[] params;

    public static RetryTaskTemplateBuilder newBuilder() {
        return new RetryTaskTemplateBuilder();
    }

    public RetryTaskTemplateBuilder withScene(String scene) {
        this.scene = scene;
        return this;
    }

    public RetryTaskTemplateBuilder withExecutorMethod(Class<? extends ExecutorMethod> executorMethod) {
        this.executorMethodClass = executorMethod;
        return this;
    }

    public RetryTaskTemplateBuilder withParam(Object params) {
        this.params = new Object[]{params};
        return this;
    }

    public SnailJobTemplate build() {
        SnailJobTemplate snailJobTemplate = new SnailJobTemplate();
        snailJobTemplate.setParams(params);
        snailJobTemplate.setExecutorMethodClass(executorMethodClass);
        snailJobTemplate.setScene(scene);
        RetryStrategy retryStrategy = SpringContext.getBeanByType(ManualRetryStrategies.class);
        snailJobTemplate.setRetryStrategy(retryStrategy);
        return snailJobTemplate;
    }
}
