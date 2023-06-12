package com.example.demo;

import com.aizuda.easy.retry.client.core.annotation.Retryable;
import com.aizuda.easy.retry.client.core.retryer.RetryType;
import com.example.client.DemoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 14:06
 */
@Component
public class TestRetryMethodService {

    @Autowired
    private DemoClient demoClient;

    @Retryable(scene = "testRetryMethod", retryMethod = MyExecutorMethod.class, retryStrategy = RetryType.ONLY_REMOTE)
    public String testRetryMethod(String p) {
        double i = 1 / 0;
        return "测试自定义重试方法";
    }

    @Retryable(scene = "testRetryHeaderTransfer", retryStrategy = RetryType.ONLY_LOCAL)
    public String testRetryHeaderTransfer(String p) {
        demoClient.get();
        double i = 1 / 0;
        return "测试重试流量标识服务间传递";
    }
}
