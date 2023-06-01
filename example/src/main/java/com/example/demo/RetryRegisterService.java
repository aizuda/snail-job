package com.example.demo;

import cn.hutool.core.lang.UUID;
import com.aizuda.easy.retry.client.core.annotation.Retryable;
import com.aizuda.easy.retry.client.core.retryer.RetryType;
import com.example.model.Cat;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-04 16:07
 */
@Service
public class RetryRegisterService {

    /**
     * 测试简单的异常情况
     */
    @Retryable(scene = "errorMethod11231")
    public String errorMethod1(List<Cat> name) {

        double i = 1 / 0;

        return "这是一个简单的异常方法";
    }

}
