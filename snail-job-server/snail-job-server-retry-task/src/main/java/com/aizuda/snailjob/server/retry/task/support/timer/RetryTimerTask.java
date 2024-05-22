package com.aizuda.snailjob.server.retry.task.support.timer;

import com.aizuda.snailjob.common.core.context.SpringContext;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.server.retry.task.support.dispatch.task.TaskActuatorFactory;
import com.aizuda.snailjob.server.retry.task.support.dispatch.task.TaskExecutor;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.TaskAccess;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.util.Timeout;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author: opensnail
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
        super.namespaceId = context.getNamespaceId();
    }

    @Override
    public void doRun(final Timeout timeout) {
        AccessTemplate accessTemplate = SpringContext.getBeanByType(AccessTemplate.class);
        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        RetryTask retryTask = retryTaskAccess.one(context.getGroupName(), context.getNamespaceId(),
                new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getNamespaceId, context.getNamespaceId())
                        .eq(RetryTask::getGroupName, context.getGroupName())
                        .eq(RetryTask::getUniqueId, context.getUniqueId())
                        .eq(RetryTask::getRetryStatus, RetryStatusEnum.RUNNING.getStatus()));
        if (Objects.isNull(retryTask)) {
            return;
        }
        TaskExecutor taskExecutor = TaskActuatorFactory.getTaskActuator(context.getScene());
        taskExecutor.actuator(retryTask);
    }
}
