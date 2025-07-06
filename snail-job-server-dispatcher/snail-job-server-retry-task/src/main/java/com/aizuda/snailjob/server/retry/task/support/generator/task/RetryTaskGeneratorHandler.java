package com.aizuda.snailjob.server.retry.task.support.generator.task;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskGeneratorDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.timer.RetryTimerContext;
import com.aizuda.snailjob.server.retry.task.support.timer.RetryTimerTask;
import com.aizuda.snailjob.server.retry.task.support.timer.RetryTimerWheel;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-01-26
 */
@Component
@RequiredArgsConstructor
public class RetryTaskGeneratorHandler {
    private final RetryTaskMapper retryTaskMapper;

    /**
     * 生成重试任务
     *
     * @param generator RetryTaskGeneratorContext
     */
    public void generateRetryTask(RetryTaskGeneratorDTO generator) {

        RetryTask retryTask = RetryTaskConverter.INSTANCE.toRetryTask(generator);
        Integer taskStatus = generator.getTaskStatus();
        if (Objects.isNull(taskStatus)) {
            taskStatus = RetryTaskStatusEnum.WAITING.getStatus();
        }
        retryTask.setTaskStatus(taskStatus);
        retryTask.setOperationReason(generator.getOperationReason());

        retryTask.setExtAttrs(StrUtil.EMPTY);
        Assert.isTrue(1 == retryTaskMapper.insert(retryTask), () -> new SnailJobServerException("Inserting retry task failed"));

        if (!RetryTaskStatusEnum.WAITING.getStatus().equals(taskStatus)) {
            return;
        }

        // 放到到时间轮
        long delay = generator.getNextTriggerAt() - DateUtils.toNowMilli();
        RetryTimerContext timerContext = RetryTaskConverter.INSTANCE.toRetryTimerContext(generator);
        timerContext.setRetryTaskId(retryTask.getId());
        RetryTimerWheel.registerWithRetry(() -> new RetryTimerTask(timerContext), Duration.ofMillis(delay));
    }
}
