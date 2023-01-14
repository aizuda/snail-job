package com.x.retry.client.core.executor;

import com.x.retry.client.core.RetryExecutor;
import com.x.retry.client.core.retryer.RetryerInfo;
import com.x.retry.client.core.strategy.RetryAnnotationMethod;
import com.x.retry.client.core.strategy.RetryMethod;
import com.x.retry.common.core.context.SpringContext;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.common.core.util.JsonUtil;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 18:08
 */
public abstract class AbstractRetryExecutor<BR, SR> implements RetryExecutor<BR, SR> {

    protected RetryerInfo retryerInfo;

    @Override
    public Object execute(Object... params) {
        return doExecute(params);
    }

    public Object doExecute(Object... params) {

        Class<? extends RetryMethod> retryMethodClass = retryerInfo.getRetryMethod();
        if (retryMethodClass.isAssignableFrom(RetryAnnotationMethod.class)) {
            LogUtils.info("执行注解重试方法：{},参数为：{}", retryMethodClass.getName(), JsonUtil.toJsonString(params));
            RetryAnnotationMethod retryAnnotationMethod = new RetryAnnotationMethod(retryerInfo);
            return retryAnnotationMethod.doExecute(params);
        } else {
            LogUtils.info("执行自定义重试方法：{},参数为：{}", retryMethodClass.getName(), JsonUtil.toJsonString(params));
            RetryMethod retryMethod = SpringContext.getBeanByType(retryMethodClass);
            return retryMethod.doExecute(params);
        }
    }

    @Override
    public RetryerInfo getRetryerInfo() {
        return retryerInfo;
    }

}
