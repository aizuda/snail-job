package com.x.retry.client.core.event;

import cn.hutool.extra.ssh.JschUtil;
import com.x.retry.common.core.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-04 16:55
 */
@Component
@Slf4j
public class SimpleXRetryListener implements XRetryListener {

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
