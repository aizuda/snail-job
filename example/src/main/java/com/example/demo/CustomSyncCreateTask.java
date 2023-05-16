package com.example.demo;

import com.aizuda.easy.retry.client.core.annotation.ExecutorMethodRegister;
import com.aizuda.easy.retry.client.core.strategy.ExecutorMethod;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 测试手动重试并同步上报任务
 *
 * @author: www.byteblogs.com
 * @date : 2022-03-07 14:07
 * @since 1.3.0
 */
@ExecutorMethodRegister(scene = CustomSyncCreateTask.SCENE, async = false, timeout = 10000, unit = TimeUnit.MILLISECONDS, forceReport = true)
@Slf4j
public class CustomSyncCreateTask implements ExecutorMethod {

    public static final String SCENE = "customSyncCreateTask";

    @Override
    public Object doExecute(Object obj) {
        log.info(SCENE + " params:[{}]", JsonUtil.toJsonString(obj));
        return "测试成功";
    }

}
