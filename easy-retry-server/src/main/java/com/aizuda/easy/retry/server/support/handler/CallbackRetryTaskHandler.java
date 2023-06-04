package com.aizuda.easy.retry.server.support.handler;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.persistence.support.RetryTaskAccess;
import com.aizuda.easy.retry.server.service.convert.RetryTaskConverter;
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

    @Transactional
    public void create(RetryTask retryTask) {
        RetryTask callbackRetryTask = RetryTaskConverter.INSTANCE.toRetryTask(retryTask);

        callbackRetryTask.setTaskType(TaskTypeEnum.CALLBACK.getType());
        retryTask.setUniqueId(SystemConstants.CALL_BACK.CB_ + retryTask.getUniqueId());
        retryTask.setRetryStatus(RetryStatusEnum.RUNNING.getStatus());
        retryTask.setRetryCount(0);
        retryTask.setCreateDt(LocalDateTime.now());
        retryTask.setUpdateDt(LocalDateTime.now());

        retryTask.setNextTriggerAt(WaitStrategies.randomWait(1, TimeUnit.SECONDS, 60, TimeUnit.SECONDS).computeRetryTime(null));

        Assert.isTrue(1 == retryTaskAccess.saveRetryTask(callbackRetryTask), () -> new EasyRetryServerException("failed to report data"));

    }

}
