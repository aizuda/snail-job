package com.example.demo;

import com.aizuda.easy.retry.client.core.strategy.ExecutorMethod;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 14:07
 */
@Component
@Slf4j
public class MyExecutorMethod implements ExecutorMethod {

    @Override
    public Object doExecute(Object params) {
        log.info("测试自定义重试方法 MyExecutorMethod params:[{}]", JsonUtil.toJsonString(params));
        return "测试成功";
    }

}
