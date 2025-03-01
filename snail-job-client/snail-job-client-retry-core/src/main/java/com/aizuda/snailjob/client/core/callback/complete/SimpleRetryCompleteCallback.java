package com.aizuda.snailjob.client.core.callback.complete;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: opensnail
 * @date : 2023-01-10 14:47
 */
@Component
@Slf4j
public class SimpleRetryCompleteCallback implements RetryCompleteCallback {

    @Override
    public void doSuccessCallback(String sceneName, String executorName, Object[] params) {

    }

    @Override
    public void doMaxRetryCallback(String sceneName, String executorName, Object[] params) {
    }
}
