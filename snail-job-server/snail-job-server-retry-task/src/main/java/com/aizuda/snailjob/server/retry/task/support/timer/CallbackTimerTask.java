package com.aizuda.snailjob.server.retry.task.support.timer;

import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.TaskAccess;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.util.Timeout;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author: opensnail
 * @date : 2023-09-22 17:09
 */
public class CallbackTimerTask extends AbstractTimerTask {
    public static final String IDEMPOTENT_KEY_PREFIX = "callback_{0}_{1}_{2}";

    private final RetryTimerContext context;

    public CallbackTimerTask(RetryTimerContext context) {
        this.context = context;
        super.groupName = context.getGroupName();
//        super.uniqueId = context.getUniqueId();
        super.namespaceId = context.getNamespaceId();
    }

    @Override
    protected void doRun(final Timeout timeout) {
        SnailJobLog.LOCAL.debug("回调任务执行 {}", LocalDateTime.now());
        AccessTemplate accessTemplate = SnailSpringContext.getBeanByType(AccessTemplate.class);
        TaskAccess<Retry> retryTaskAccess = accessTemplate.getRetryAccess();
        Retry retry = retryTaskAccess.one(new LambdaQueryWrapper<Retry>()
                        .eq(Retry::getNamespaceId, context.getNamespaceId())
                        .eq(Retry::getGroupName, context.getGroupName())
                        .eq(Retry::getId, context.getRetryId())
                        .eq(Retry::getRetryStatus, RetryStatusEnum.RUNNING.getStatus()));
        if (Objects.isNull(retry)) {
            return;
        }
//        TaskExecutor taskExecutor = TaskActuatorFactory.getTaskActuator(context.getScene());
//        taskExecutor.actuator(retry);
    }

    @Override
    public String idempotentKey() {
        return MessageFormat.format(IDEMPOTENT_KEY_PREFIX, context.getGroupName(), context.getNamespaceId(), context.getRetryTaskId());
    }
}
