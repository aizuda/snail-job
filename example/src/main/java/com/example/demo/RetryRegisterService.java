package com.example.demo;

import cn.hutool.core.lang.UUID;
import com.aizuda.easy.retry.client.core.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-04 16:07
 */
@Service
public class RetryRegisterService {

    /**
     * 测试简单的异常情况
     */
    @Retryable(scene = "errorMethod1", localTimes = 3)
    public String errorMethod1(String name) {

        double i = 1 / 0;

        return "这是一个简单的异常方法";
    }


}
