package com.aizuda.snailjob.client.core.strategy;

import com.aizuda.snailjob.client.core.retryer.RetryerInfo;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

/**
 * @author: opensnail
 * @date : 2022-03-04 17:18
 */
@Slf4j
public class ExecutorAnnotationMethod implements ExecutorMethod {

    private RetryerInfo retryerInfo;

    public ExecutorAnnotationMethod(RetryerInfo retryerInfo) {
        this.retryerInfo = retryerInfo;
    }

    @Override
    public Object doExecute(Object params) {
        Class<?>[] paramTypes = retryerInfo.getMethod().getParameterTypes();
       SnailJobLog.LOCAL.info("执行原重试方法：[{}],参数为：[{}]", retryerInfo.getExecutorClassName(), JsonUtil.toJsonString(params));

        if (paramTypes.length > 0) {
            return ReflectionUtils.invokeMethod(retryerInfo.getMethod(), retryerInfo.getExecutor(), (Object[]) params);
        } else {
            return ReflectionUtils.invokeMethod(retryerInfo.getMethod(), retryerInfo.getExecutor());
        }
    }
}
