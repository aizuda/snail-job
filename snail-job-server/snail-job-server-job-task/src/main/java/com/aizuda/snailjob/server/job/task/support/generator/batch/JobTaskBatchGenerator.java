package com.aizuda.snailjob.server.job.task.support.generator.batch;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerTask;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
                CollectionUtils.isEmpty(CacheRegisterTable.getServerNodeSet(context.getGroupName(), context.getNamespaceId()))) {
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

        // 非待处理状态无需进入时间轮中
        if (JobTaskBatchStatusEnum.WAITING.getStatus() != jobTaskBatch.getTaskBatchStatus()) {
            WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
            taskExecuteDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
            taskExecuteDTO.setTaskExecutorScene(context.getTaskExecutorScene());
            taskExecuteDTO.setParentId(context.getWorkflowNodeId());
            taskExecuteDTO.setTaskBatchId(jobTaskBatch.getId());
            workflowBatchHandler.openNextNode(taskExecuteDTO);

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
        JobTimerWheel.register(SyetemTaskTypeEnum.JOB.getType(), jobTaskBatch.getId(),
                new JobTimerTask(jobTimerTaskDTO), delay, TimeUnit.MILLISECONDS);

        return jobTaskBatch;
    }

}
