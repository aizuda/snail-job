package com.aizuda.easy.retry.server.support.handler;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.server.config.SystemProperties;
import com.aizuda.easy.retry.server.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTaskLog;
import com.aizuda.easy.retry.server.persistence.support.RetryTaskAccess;
import com.aizuda.easy.retry.server.service.convert.RetryTaskConverter;
import com.aizuda.easy.retry.server.service.convert.RetryTaskLogConverter;
import com.aizuda.easy.retry.server.support.strategy.WaitStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


/**
 * @author www.byteblogs.com
 * @date 2023-06-04
 * @since 1.5.0
 */
@Component
public class CallbackRetryTaskHandler {

    @Autowired
    @Qualifier("retryTaskAccessProcessor")
    private RetryTaskAccess<RetryTask> retryTaskAccess;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;
    @Autowired
    private SystemProperties systemProperties;

    @Transactional
    public void create(RetryTask retryTask) {
        if (!TaskTypeEnum.RETRY.getType().equals(retryTask.getTaskType())) {
          return;
        }

        RetryTask callbackRetryTask = RetryTaskConverter.INSTANCE.toRetryTask(retryTask);

        callbackRetryTask.setTaskType(TaskTypeEnum.CALLBACK.getType());
        callbackRetryTask.setId(null);
        callbackRetryTask.setUniqueId(systemProperties.getCallback().getPrefix() + retryTask.getUniqueId());
        callbackRetryTask.setRetryStatus(RetryStatusEnum.RUNNING.getStatus());
        callbackRetryTask.setRetryCount(0);
        callbackRetryTask.setCreateDt(LocalDateTime.now());
        callbackRetryTask.setUpdateDt(LocalDateTime.now());

        callbackRetryTask.setNextTriggerAt(WaitStrategies.randomWait(1, TimeUnit.SECONDS, 60, TimeUnit.SECONDS).computeRetryTime(null));

        Assert.isTrue(1 == retryTaskAccess.saveRetryTask(callbackRetryTask), () -> new EasyRetryServerException("failed to report data"));

        // 初始化回调日志
        RetryTaskLog retryTaskLog = RetryTaskLogConverter.INSTANCE.toRetryTask(callbackRetryTask);
        // 记录重试日志
        retryTaskLog.setTaskType(TaskTypeEnum.CALLBACK.getType());
        retryTaskLog.setCreateDt(LocalDateTime.now());
        Assert.isTrue(1 ==  retryTaskLogMapper.insert(retryTaskLog),
            () -> new EasyRetryServerException("新增重试日志失败"));

    }

}
