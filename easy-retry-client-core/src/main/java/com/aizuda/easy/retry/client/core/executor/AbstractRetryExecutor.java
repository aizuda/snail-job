package com.aizuda.easy.retry.client.core.executor;

import com.aizuda.easy.retry.client.core.RetryExecutor;
import com.aizuda.easy.retry.client.core.retryer.RetryerInfo;
import com.aizuda.easy.retry.client.core.strategy.RetryAnnotationMethod;
import com.aizuda.easy.retry.client.core.strategy.RetryMethod;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: www.byteblogs.com
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

        Class<? extends RetryMethod> retryMethodClass = retryerInfo.getRetryMethod();
        if (retryMethodClass.isAssignableFrom(RetryAnnotationMethod.class)) {
            LogUtils.info(log, "执行注解重试方法：{},参数为：{}", retryMethodClass.getName(), JsonUtil.toJsonString(params));
            RetryAnnotationMethod retryAnnotationMethod = new RetryAnnotationMethod(retryerInfo);
            return retryAnnotationMethod.doExecute(params);
        } else {
            LogUtils.info(log, "执行自定义重试方法：{},参数为：{}", retryMethodClass.getName(), JsonUtil.toJsonString(params));
            RetryMethod retryMethod = SpringContext.getBeanByType(retryMethodClass);
            return retryMethod.doExecute(params);
        }
    }

    @Override
    public RetryerInfo getRetryerInfo() {
        return retryerInfo;
    }

}
