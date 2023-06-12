package com.example.controller;

import com.aizuda.easy.retry.client.core.annotation.Retryable;
import com.aizuda.easy.retry.client.core.retryer.RetryType;
import com.aizuda.easy.retry.common.core.model.Result;
import com.example.client.DemoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author: www.byteblogs.com
 * @date : 2022-05-19 08:21
 */
@RequestMapping("/ddl")
@RestController
public class TestDdlController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DemoClient demoClient;

    @GetMapping("rest-template")
    public Result testDDL() {
        Result result = restTemplate.getForObject("http://127.0.0.1:8088/school/id", Result.class);

        if (result.getStatus() == 0) {
            throw new UnsupportedOperationException(result.getMessage());
        }
        return result;
    }

    @GetMapping("test-feign")
    public Result feign() {
        Result result = demoClient.get();
        if (result.getStatus() == 0) {
            throw new UnsupportedOperationException(result.getMessage());
        }
        return result;

    }

    @GetMapping("test-retry-header-controller-transfer")
    @Retryable(scene = "testRetryHeaderControllerTransfer", retryStrategy = RetryType.ONLY_LOCAL)
    public Result testRetryHeaderTransfer() {
        Result result = demoClient.get();
        if (result.getStatus() == 0) {
            throw new UnsupportedOperationException(result.getMessage());
        }
        return result;

    }
}
