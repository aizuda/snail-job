package com.aizuda.snailjob.server.job.task.support.timer;

import com.aizuda.snailjob.common.core.context.SpringContext;
import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.server.job.task.support.alarm.event.WorkflowTaskFailAlarmEvent;
import com.aizuda.snailjob.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowTaskBatch;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.AllArgsConstructor;

import java.util.Objects;

/**
 * @author opensnail
 * @date 2024-05-20 22:25:12
 * @since sj_1.0.0
 */
@AllArgsConstructor
public class WorkflowTimeoutCheckTask implements TimerTask {
    private final Long workflowTaskBatchId;
    @Override
    public void run(Timeout timeout) throws Exception {
        WorkflowTaskBatchMapper workflowTaskBatchMapper = SpringContext.getBean(WorkflowTaskBatchMapper.class);
        WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchMapper.selectById(workflowTaskBatchId);
        // 幂等检查
        if (Objects.isNull(workflowTaskBatch) || JobTaskBatchStatusEnum.COMPLETED.contains(workflowTaskBatch.getTaskBatchStatus())) {
            return;
        }

        WorkflowBatchHandler workflowBatchHandler = SpringContext.getBean(WorkflowBatchHandler.class);

        // 超时停止任务
        workflowBatchHandler.stop(workflowTaskBatchId, JobOperationReasonEnum.TASK_EXECUTION_TIMEOUT.getReason());
        SpringContext.getContext().publishEvent(new WorkflowTaskFailAlarmEvent(workflowTaskBatchId));
    }
}
