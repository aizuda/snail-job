package com.aizuda.easy.retry.server.job.task.support.generator.batch;

import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.easy.retry.server.common.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.timer.JobTimerTask;
import com.aizuda.easy.retry.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 10:22:26
 * @since 2.4.0
 */
@Component
@Slf4j
public class JobTaskBatchGenerator {

    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;

    @Transactional
    public JobTaskBatch generateJobTaskBatch(JobTaskBatchGeneratorContext context) {

        // 生成一个新的任务
        JobTaskBatch jobTaskBatch = JobTaskConverter.INSTANCE.toJobTaskBatch(context);
        JobTaskExecutorSceneEnum jobTaskExecutorSceneEnum = JobTaskExecutorSceneEnum.get(
            context.getTaskExecutorScene());
        jobTaskBatch.setTaskType(jobTaskExecutorSceneEnum.getTaskType().getType());
        jobTaskBatch.setCreateDt(LocalDateTime.now());

        // 无执行的节点
        if (Objects.isNull(context.getOperationReason()) && Objects.isNull(context.getTaskBatchStatus()) &&
                CollectionUtils.isEmpty(CacheRegisterTable.getServerNodeSet(context.getGroupName(), context.getNamespaceId()))) {
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.CANCEL.getStatus());
            jobTaskBatch.setOperationReason(JobOperationReasonEnum.NOT_CLIENT.getReason());

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (Objects.nonNull(context.getWorkflowNodeId()) && Objects.nonNull(context.getWorkflowTaskBatchId())) {
                        // 若是工作流则开启下一个任务
                        try {
                            WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
                            taskExecuteDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
                            taskExecuteDTO.setTaskExecutorScene(JobTaskExecutorSceneEnum.AUTO_JOB.getType());
                            taskExecuteDTO.setParentId(context.getWorkflowNodeId());
                            ActorRef actorRef = ActorGenerator.workflowTaskExecutorActor();
                            actorRef.tell(taskExecuteDTO, actorRef);
                        } catch (Exception e) {
                            log.error("任务调度执行失败", e);
                        }
                    }
                }
            });

        } else {
            // 生成一个新的任务
            jobTaskBatch.setTaskBatchStatus(Optional.ofNullable(context.getTaskBatchStatus()).orElse(JobTaskBatchStatusEnum.WAITING.getStatus()));
            jobTaskBatch.setOperationReason(context.getOperationReason());
        }

        try {
            Assert.isTrue(1 == jobTaskBatchMapper.insert(jobTaskBatch), () -> new EasyRetryServerException("新增调度任务失败.jobId:[{}]", context.getJobId()));
        } catch (DuplicateKeyException ignored) {
            // 忽略重复的DAG任务
            return jobTaskBatchMapper.selectOne(new LambdaQueryWrapper<JobTaskBatch>()
                .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
                .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
            );

        }

        // 非待处理状态无需进入时间轮中
        if (JobTaskBatchStatusEnum.WAITING.getStatus() != jobTaskBatch.getTaskBatchStatus()) {
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
        JobTimerWheel.register(TaskTypeEnum.JOB.getType(), jobTaskBatch.getId(),
                new JobTimerTask(jobTimerTaskDTO), delay, TimeUnit.MILLISECONDS);

        return jobTaskBatch;
    }

}
