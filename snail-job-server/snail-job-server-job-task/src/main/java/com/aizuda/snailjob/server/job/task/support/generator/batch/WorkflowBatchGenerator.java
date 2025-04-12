package com.aizuda.snailjob.server.job.task.support.generator.batch;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.WorkflowTimerTaskDTO;
import com.aizuda.snailjob.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.snailjob.server.job.task.support.timer.WorkflowTimerTask;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowTaskBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * @author: xiaowoniu
 * @date : 2023-12-22 09:04
 * @since : 2.6.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowBatchGenerator {
    private final WorkflowTaskBatchMapper workflowTaskBatchMapper;

    public void generateJobTaskBatch(WorkflowTaskBatchGeneratorContext context) {

        // 生成任务批次
        WorkflowTaskBatch workflowTaskBatch = WorkflowTaskConverter.INSTANCE.toWorkflowTaskBatch(context);
        workflowTaskBatch.setTaskBatchStatus(Optional.ofNullable(context.getTaskBatchStatus()).orElse(JobTaskBatchStatusEnum.WAITING.getStatus()));
        workflowTaskBatch.setOperationReason(context.getOperationReason());
        workflowTaskBatch.setWfContext(context.getWfContext());

        Assert.isTrue(1 == workflowTaskBatchMapper.insert(workflowTaskBatch), () -> new SnailJobServerException("Adding new scheduling task failed. [{}]", context.getWorkflowId()));

        // 非待处理状态无需进入时间轮中
        if (JobTaskBatchStatusEnum.WAITING.getStatus() != workflowTaskBatch.getTaskBatchStatus()) {
            return;
        }

        // 开始执行工作流
        // 进入时间轮
        long delay = context.getNextTriggerAt() - DateUtils.toNowMilli();
        WorkflowTimerTaskDTO workflowTimerTaskDTO = new WorkflowTimerTaskDTO();
        workflowTimerTaskDTO.setWorkflowTaskBatchId(workflowTaskBatch.getId());
        workflowTimerTaskDTO.setWorkflowId(context.getWorkflowId());
        workflowTimerTaskDTO.setTaskExecutorScene(context.getTaskExecutorScene());

        JobTimerWheel.registerWithWorkflow(() -> new WorkflowTimerTask(workflowTimerTaskDTO), Duration.ofMillis(delay));
    }
}
