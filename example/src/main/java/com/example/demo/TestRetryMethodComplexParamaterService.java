package com.example.demo;

import com.example.model.Dog;
import com.example.model.Zoo;
import com.x.retry.client.core.annotation.Retryable;
import com.x.retry.client.core.retryer.RetryType;
import com.x.retry.common.core.log.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 14:06
 */
@Component
@Slf4j
public class TestRetryMethodComplexParamaterService {

    @Retryable(scene = "testRetryMethodForZoo")
    public String testRetryMethod(Zoo zoo) {
        LogUtils.debug(log,"testRetryMethodForZoo zoo:[{}]", zoo);
        double i = 1 / 0;

        return "测试自定义重试方法";
    }

    @Retryable(scene = "testRetryMethodForZooList")
    public String testRetryMethod(List<Zoo> zoo, Dog dog) {
        LogUtils.debug(log, "testRetryMethodForZooList zoo:[{}] dog:[{}]", zoo, dog);
        double i = 1 / 0;
        return "测试自定义重试方法";
    }


}
