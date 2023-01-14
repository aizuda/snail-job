package com.example.demo;

import com.x.retry.client.core.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 14:06
 */
@Component
public class TestRetryMethodService {

    @Retryable(scene = "testRetryMethod", retryMethod = MyRetryMethod.class)
    public String testRetryMethod(String p) {
        return "测试自定义重试方法";
    }


}
