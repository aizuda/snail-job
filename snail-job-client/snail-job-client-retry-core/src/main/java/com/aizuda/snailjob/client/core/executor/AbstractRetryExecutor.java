package com.aizuda.snailjob.client.core.executor;

import com.aizuda.snailjob.client.core.RetryExecutor;
import com.aizuda.snailjob.client.core.retryer.RetryerInfo;
import com.aizuda.snailjob.client.core.strategy.ExecutorMethod;
import com.aizuda.snailjob.client.core.strategy.ExecutorAnnotationMethod;
import com.aizuda.snailjob.common.core.context.SpringContext;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.client.core.retryer.RetryerInfo;
import com.aizuda.snailjob.client.core.strategy.ExecutorAnnotationMethod;
import com.aizuda.snailjob.client.core.strategy.ExecutorMethod;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: opensnail
 * @date : 2022-03-03 18:08
 */
@Slf4j
public abstract class AbstractRetryExecutor<BR, SR> implements RetryExecutor<BR, SR> {

    protected RetryerInfo retryerInfo;

    @Override
    public Object execute(Object... params) {
        return doExecute(params);
    }

    public Object doExecute(Object... params) {

        Class<? extends ExecutorMethod> retryMethodClass = retryerInfo.getExecutorMethod();
        if (retryMethodClass.isAssignableFrom(ExecutorAnnotationMethod.class)) {
           SnailJobLog.LOCAL.debug("执行注解重试方法：{},参数为：{}", retryMethodClass.getName(), JsonUtil.toJsonString(params));
            ExecutorAnnotationMethod retryAnnotationMethod = new ExecutorAnnotationMethod(retryerInfo);
            return retryAnnotationMethod.doExecute(params);
        } else {
           SnailJobLog.LOCAL.debug("执行自定义重试方法：{},参数为：{}", retryMethodClass.getName(), JsonUtil.toJsonString(params));
            ExecutorMethod executorMethod = SpringContext.getBeanByType(retryMethodClass);
            return executorMethod.doExecute(params);
        }
    }

    @Override
    public RetryerInfo getRetryerInfo() {
        return retryerInfo;
    }

}
