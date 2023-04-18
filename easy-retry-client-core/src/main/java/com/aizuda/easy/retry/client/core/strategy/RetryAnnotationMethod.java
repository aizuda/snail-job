package com.aizuda.easy.retry.client.core.strategy;

import com.aizuda.easy.retry.client.core.retryer.RetryerInfo;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-04 17:18
 */
@Slf4j
public class RetryAnnotationMethod implements RetryMethod {

    private RetryerInfo retryerInfo;

    public RetryAnnotationMethod(RetryerInfo retryerInfo) {
        this.retryerInfo = retryerInfo;
    }

    @Override
    public Object doExecute(Object[] params) {
        Class<?>[] paramTypes = retryerInfo.getExecutorMethod().getParameterTypes();
        LogUtils.info(log, "执行原重试方法：[{}],参数为：[{}]", retryerInfo.getExecutorClassName(), JsonUtil.toJsonString(params));

        if (paramTypes.length > 0) {
            return ReflectionUtils.invokeMethod(retryerInfo.getExecutorMethod(), retryerInfo.getExecutor(), params);
        } else {
            return ReflectionUtils.invokeMethod(retryerInfo.getExecutorMethod(), retryerInfo.getExecutor());
        }
    }
}
