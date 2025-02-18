package com.aizuda.snailjob.server.retry.task.support.timer;

import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.TimerTask;
import com.aizuda.snailjob.server.retry.task.dto.TaskStopJobDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.handler.RetryTaskStopHandler;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import io.netty.util.Timeout;
import lombok.AllArgsConstructor;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * 任务超时检查
 *
 * @author opensnail
 * @date 2024-05-20 21:16:09
 * @since sj_1.0.0
 */
@AllArgsConstructor
public class RetryTimeoutCheckTask implements TimerTask<String> {
    private static final String IDEMPOTENT_KEY_PREFIX = "retry_timeout_check_{0}";

    private final Long retryTaskId;
    private final Long retryId;
    private final RetryTaskStopHandler retryTaskStopHandler;
    private final RetryMapper retryMapper;
    private final RetryTaskMapper retryTaskMapper;

    @Override
    public void run(Timeout timeout) throws Exception {
        RetryTimerWheel.clearCache(idempotentKey());
        RetryTask retryTask = retryTaskMapper.selectById(retryTaskId);
        if (Objects.isNull(retryTask)) {
            SnailJobLog.LOCAL.error("retryTaskId:[{}] 不存在", retryTaskId);
            return;
        }

        // 已经完成了，无需重复停止任务
        if (RetryTaskStatusEnum.TERMINAL_STATUS_SET.contains(retryTask.getTaskStatus())) {
            return;
        }

        Retry retry = retryMapper.selectById(retryId);
        if (Objects.isNull(retry)) {
            SnailJobLog.LOCAL.error("retryId:[{}]不存在", retryId);
            return;
        }

        // 超时停止任务
        String reason = "超时中断.retryTaskId:[" + retryTaskId + "]";

        TaskStopJobDTO stopJobDTO = RetryTaskConverter.INSTANCE.toTaskStopJobDTO(retry);
        stopJobDTO.setRetryTaskId(retryTaskId);
        stopJobDTO.setRetryId(retryId);
        stopJobDTO.setOperationReason(JobOperationReasonEnum.TASK_EXECUTION_TIMEOUT.getReason());
        stopJobDTO.setNeedUpdateTaskStatus(true);
        retryTaskStopHandler.stop(stopJobDTO);

        SnailJobLog.LOCAL.info(reason);
    }

    @Override
    public String idempotentKey() {
        return MessageFormat.format(IDEMPOTENT_KEY_PREFIX, retryId);
    }
}
