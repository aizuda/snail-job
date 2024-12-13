package com.aizuda.snailjob.server.job.task.support.generator.batch;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.JobNotifySceneEnum;
import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.JobTaskFailAlarmEventDTO;
import com.aizuda.snailjob.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.snailjob.server.job.task.dto.TaskExecuteDTO;
import com.aizuda.snailjob.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.alarm.event.JobTaskFailNodeAlarmEvent;
import com.aizuda.snailjob.server.job.task.support.handler.JobTaskBatchHandler;
import com.aizuda.snailjob.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerTask;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * @author opensnail
 * @date 2023-10-02 10:22:26
 * @since 2.4.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JobTaskBatchGenerator {

    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final WorkflowBatchHandler workflowBatchHandler;
    private final JobTaskBatchHandler jobTaskBatchHandler;
    private final JobMapper jobMapper;

    @Transactional
    public JobTaskBatch generateJobTaskBatch(JobTaskBatchGeneratorContext context) {

        // 生成一个新的任务
        JobTaskBatch jobTaskBatch = JobTaskConverter.INSTANCE.toJobTaskBatch(context);
        JobTaskExecutorSceneEnum jobTaskExecutorSceneEnum = JobTaskExecutorSceneEnum.get(
                context.getTaskExecutorScene());
        jobTaskBatch.setSystemTaskType(jobTaskExecutorSceneEnum.getSystemTaskType().getType());
        jobTaskBatch.setCreateDt(LocalDateTime.now());

        // 无执行的节点
        if (Objects.isNull(context.getOperationReason()) && Objects.isNull(context.getTaskBatchStatus()) &&
                CollUtil.isEmpty(CacheRegisterTable.getServerNodeSet(context.getGroupName(), context.getNamespaceId()))) {
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.CANCEL.getStatus());
            jobTaskBatch.setOperationReason(JobOperationReasonEnum.NOT_CLIENT.getReason());
        } else {
            // 生成一个新的任务
            jobTaskBatch.setTaskBatchStatus(Optional.ofNullable(context.getTaskBatchStatus()).orElse(JobTaskBatchStatusEnum.WAITING.getStatus()));
            jobTaskBatch.setOperationReason(context.getOperationReason());
        }

        try {
            Assert.isTrue(1 == jobTaskBatchMapper.insert(jobTaskBatch), () -> new SnailJobServerException("新增调度任务失败.jobId:[{}]", context.getJobId()));
        } catch (DuplicateKeyException ignored) {
            // 忽略重复的DAG任务
            return jobTaskBatchMapper.selectOne(new LambdaQueryWrapper<JobTaskBatch>()
                    .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
                    .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
            );
        }

        // 无客户端节点-告警通知
        if (JobTaskBatchStatusEnum.CANCEL.getStatus() == jobTaskBatch.getTaskBatchStatus() && JobOperationReasonEnum.NOT_CLIENT.getReason() == jobTaskBatch.getOperationReason()) {
            SnailSpringContext.getContext().publishEvent(
                    new JobTaskFailNodeAlarmEvent(JobTaskFailAlarmEventDTO.builder()
                            .jobTaskBatchId(jobTaskBatch.getId())
                            .reason(JobNotifySceneEnum.JOB_NO_CLIENT_NODES_ERROR.getDesc()).build()));
        }

        // 非待处理状态无需进入时间轮中
        if (JobTaskBatchStatusEnum.WAITING.getStatus() != jobTaskBatch.getTaskBatchStatus()) {

            // 开启下一个工作流
            openNextWorkflow(context, jobTaskBatch);

            // 若是常驻任务则需要再次进入时间轮
            openNextResidentTask(context, jobTaskBatch);
            return jobTaskBatch;
        }

        // 进入时间轮
        long delay = context.getNextTriggerAt() - DateUtils.toNowMilli();
        JobTimerTaskDTO jobTimerTaskDTO = new JobTimerTaskDTO();
        jobTimerTaskDTO.setTaskBatchId(jobTaskBatch.getId());
        jobTimerTaskDTO.setJobId(context.getJobId());
        jobTimerTaskDTO.setTaskExecutorScene(context.getTaskExecutorScene());
        jobTimerTaskDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
        jobTimerTaskDTO.setWorkflowNodeId(context.getWorkflowNodeId());
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    JobTimerWheel.registerWithJob(() -> new JobTimerTask(jobTimerTaskDTO), Duration.ofMillis(delay));
                }
            });
        } else {
            JobTimerWheel.registerWithJob(() -> new JobTimerTask(jobTimerTaskDTO), Duration.ofMillis(delay));
        }
        return jobTaskBatch;
    }

    private void openNextResidentTask(JobTaskBatchGeneratorContext context, JobTaskBatch jobTaskBatch) {

        // 手动触发的定时任务、工作流场景下不存在常驻任务，无需触发
        if (JobTaskExecutorSceneEnum.MANUAL_JOB.getType().equals(context.getTaskExecutorScene())
                || JobTaskExecutorSceneEnum.AUTO_WORKFLOW.getType().equals(context.getTaskExecutorScene())
                || JobTaskExecutorSceneEnum.MANUAL_WORKFLOW.getType().equals(context.getTaskExecutorScene())) {
            return;
        }

        TaskExecuteDTO taskExecuteDTO = new TaskExecuteDTO();
        taskExecuteDTO.setTaskBatchId(jobTaskBatch.getId());
        taskExecuteDTO.setJobId(context.getJobId());
        taskExecuteDTO.setTaskExecutorScene(context.getTaskExecutorScene());
        taskExecuteDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
        taskExecuteDTO.setWorkflowNodeId(context.getWorkflowNodeId());
        Job job = jobMapper.selectById(context.getJobId());
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    jobTaskBatchHandler.openResidentTask(job, taskExecuteDTO);
                }
            });
        } else {
            jobTaskBatchHandler.openResidentTask(job, taskExecuteDTO);
        }

    }

    private void openNextWorkflow(JobTaskBatchGeneratorContext context, JobTaskBatch jobTaskBatch) {
        WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
        taskExecuteDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
        taskExecuteDTO.setTaskExecutorScene(context.getTaskExecutorScene());
        taskExecuteDTO.setParentId(context.getWorkflowNodeId());
        taskExecuteDTO.setTaskBatchId(jobTaskBatch.getId());
        workflowBatchHandler.openNextNode(taskExecuteDTO);
    }

}
