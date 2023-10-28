package com.aizuda.easy.retry.server.retry.task.support.timer;

import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.task.TaskActuatorFactory;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.task.TaskExecutor;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.access.TaskAccess;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-22 17:09
 */
@Slf4j
public class CallbackTimerTask extends AbstractTimerTask {

    private RetryTimerContext context;

    public CallbackTimerTask(RetryTimerContext context) {
        this.context = context;
        super.groupName = context.getGroupName();
        super.uniqueId = context.getUniqueId();
    }

    @Override
    protected void doRun(final Timeout timeout) {
        log.info("回调任务执行 {}", LocalDateTime.now());
        AccessTemplate accessTemplate = SpringContext.getBeanByType(AccessTemplate.class);
        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        RetryTask retryTask = retryTaskAccess.one(context.getGroupName(), new LambdaQueryWrapper<RetryTask>()
            .eq(RetryTask::getGroupName, context.getGroupName())
            .eq(RetryTask::getUniqueId, context.getUniqueId())
            .eq(RetryTask::getRetryStatus, RetryStatusEnum.RUNNING.getStatus()));
        TaskExecutor taskExecutor = TaskActuatorFactory.getTaskActuator(context.getScene());
        taskExecutor.actuator(retryTask);
    }

}