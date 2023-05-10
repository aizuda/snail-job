package com.example.demo;

import com.aizuda.easy.retry.client.core.annotation.ExecutorMethodRegister;
import com.aizuda.easy.retry.client.core.strategy.ExecutorMethod;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.example.model.Cat;
import com.example.model.Zoo;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 14:07
 */
@ExecutorMethodRegister(scene = CustomCreateTask.SCENE)
@Slf4j
public class CustomCreateTask implements ExecutorMethod {

    public static final String SCENE = "customCreateTask";

    @Override
    public Object doExecute(Object obj) {
        log.info("测试自定义重试方法 MyExecutorMethod params:[{}]", JsonUtil.toJsonString(obj));
        return "测试成功";
    }

}
