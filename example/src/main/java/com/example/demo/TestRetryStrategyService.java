package com.example.demo;

import com.aizuda.easy.retry.client.core.annotation.Retryable;
import com.aizuda.easy.retry.client.core.retryer.RetryType;
import org.springframework.stereotype.Component;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 14:03
 */
@Component
public class TestRetryStrategyService {

    /**
     * 测试仅内存重试和远程重试
     */
    @Retryable(scene = "errorMethodForLocalAndRemote", localTimes = 3, retryStrategy = RetryType.LOCAL_REMOTE)
    public String errorMethodForLocalAndRemote(String name) {

        double i = 1 / 0;

        return "这是一个简单的异常方法";
    }

    /**
     * 测试仅内存重试
     */
    @Retryable(scene = "errorMethodForOnlyLocal", localTimes = 3, retryStrategy = RetryType.ONLY_LOCAL)
    public String errorMethodForOnlyLocal(String name) {

        double i = 1 / 0;

        return "测试仅内存重试";
    }

    /**
     * 测试仅内存重试
     */
    @Retryable(scene = "errorMethodForOnlyRemote", bizNo = "#name", localTimes = 3, retryStrategy = RetryType.ONLY_REMOTE)
    public String errorMethodForOnlyRemote(String name) {

        double i = 1 / 0;

        return "测试仅内存重试";
    }

}
