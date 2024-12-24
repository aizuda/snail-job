package com.aizuda.snailjob.server.job.task.support.timer;

import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.JobNotifySceneEnum;
import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.TimerTask;
import com.aizuda.snailjob.server.job.task.dto.WorkflowTaskFailAlarmEventDTO;
import com.aizuda.snailjob.server.job.task.support.alarm.event.WorkflowTaskFailAlarmEvent;
import com.aizuda.snailjob.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowTaskBatch;
import io.netty.util.Timeout;
import lombok.AllArgsConstructor;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * @author opensnail
 * @date 2024-05-20 22:25:12
 * @since sj_1.0.0
 */
@AllArgsConstructor
public class WorkflowTimeoutCheckTask implements TimerTask<String> {
    private static final String IDEMPOTENT_KEY_PREFIX = "workflow_timeout_check_{0}";

    private final Long workflowTaskBatchId;

    @Override
    public void run(Timeout timeout) throws Exception {
        JobTimerWheel.clearCache(idempotentKey());
        WorkflowTaskBatchMapper workflowTaskBatchMapper = SnailSpringContext.getBean(WorkflowTaskBatchMapper.class);
        WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchMapper.selectById(workflowTaskBatchId);
        // 幂等检查
        if (Objects.isNull(workflowTaskBatch) || JobTaskBatchStatusEnum.COMPLETED.contains(workflowTaskBatch.getTaskBatchStatus())) {
            return;
        }

        WorkflowBatchHandler workflowBatchHandler = SnailSpringContext.getBean(WorkflowBatchHandler.class);

        // 超时停止任务
        workflowBatchHandler.stop(workflowTaskBatchId, JobOperationReasonEnum.TASK_EXECUTION_TIMEOUT.getReason());

        String reason = String.format("超时中断.workflowTaskBatchId:[%s]", workflowTaskBatchId);
        SnailSpringContext.getContext().publishEvent(new WorkflowTaskFailAlarmEvent(WorkflowTaskFailAlarmEventDTO.builder()
                .workflowTaskBatchId(workflowTaskBatchId)
                .notifyScene(JobNotifySceneEnum.WORKFLOW_TASK_ERROR.getNotifyScene())
                .reason(reason)
                .build()));

        SnailJobLog.LOCAL.info(reason);
    }

    @Override
    public String idempotentKey() {
        return MessageFormat.format(IDEMPOTENT_KEY_PREFIX, workflowTaskBatchId);
    }
}
