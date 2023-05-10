package com.example.demo;

import com.aizuda.easy.retry.client.core.annotation.Retryable;
import com.aizuda.easy.retry.client.core.retryer.RetryType;
import org.springframework.stereotype.Component;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 14:06
 */
@Component
public class TestRetryMethodService {

    @Retryable(scene = "testRetryMethod", retryMethod = MyExecutorMethod.class, retryStrategy = RetryType.ONLY_REMOTE)
    public String testRetryMethod(String p) {
        double i = 1 / 0;
        return "测试自定义重试方法";
    }

}
