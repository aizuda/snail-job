package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor;

import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.task.TaskActuator;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.task.TaskActuatorFactory;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.task.TaskActuatorSceneEnum;
import com.aizuda.easy.retry.server.retry.task.support.retry.RetryExecutor;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.access.TaskAccess;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-22 17:09
 */
@Data
@Slf4j
public class RetryTimerTask extends AbstractTimerTask {

    private RetryTimerContext context;

    public RetryTimerTask(RetryTimerContext context) {
        this.context = context;
        super.groupName = context.getGroupName();
        super.uniqueId = context.getUniqueId();
    }

    @Override
    public void doRun(final Timeout timeout){
        log.info("重试任务执行 {}", LocalDateTime.now());
        // todo
        AccessTemplate accessTemplate = SpringContext.getBeanByType(AccessTemplate.class);
        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        RetryTask retryTask = retryTaskAccess.one(context.getGroupName(), new LambdaQueryWrapper<RetryTask>()
                .eq(RetryTask::getGroupName, context.getGroupName())
                .eq(RetryTask::getUniqueId, context.getUniqueId())
                .eq(RetryTask::getRetryStatus, RetryStatusEnum.RUNNING.getStatus()));
        TaskActuator taskActuator = TaskActuatorFactory.getTaskActuator(context.getScene());
        taskActuator.actuator(retryTask);
    }
}
