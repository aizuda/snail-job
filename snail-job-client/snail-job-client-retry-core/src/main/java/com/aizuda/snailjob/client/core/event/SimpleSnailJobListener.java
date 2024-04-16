package com.aizuda.snailjob.client.core.event;

import com.aizuda.snailjob.common.core.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: opensnail
 * @date : 2022-03-04 16:55
 */
@Slf4j
public class SimpleSnailJobListener implements SnailJobListener {

    @Override
    public void beforeRetry(String sceneName, String executorClassName, Object[] params) {
        log.debug("------> beforeRetry sceneName:[{}] executorClassName:[{}] params:[{}]",
                sceneName, executorClassName, JsonUtil.toJsonString(params));
    }

    @Override
    public void successOnRetry(Object result, String sceneName, String executorClassName) {
        log.debug("------> successOnRetry sceneName:[{}] executorClassName:[{}] result:[{}]",
                sceneName, executorClassName, JsonUtil.toJsonString(result));
    }

    @Override
    public void failureOnRetry(String sceneName, String executorClassName, Throwable e) {
        log.debug("------> failureOnRetry sceneName:[{}] executorClassName:[{}]", sceneName, executorClassName, e);
    }
}
