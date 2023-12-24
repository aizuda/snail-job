package com.aizuda.easy.retry.server.job.task.support.handler;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.enums.JobTriggerTypeEnum;
import com.aizuda.easy.retry.server.job.task.dto.CompleteJobBatchDTO;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.easy.retry.server.job.task.support.event.JobTaskFailAlarmEvent;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-10 16:50
 */
@Component
@Slf4j
public class JobTaskBatchHandler {

    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;

    public boolean complete(CompleteJobBatchDTO completeJobBatchDTO) {

        List<JobTask> jobTasks = jobTaskMapper.selectList(
            new LambdaQueryWrapper<JobTask>()
                    .select(JobTask::getTaskStatus, JobTask::getResultMessage)
                .eq(JobTask::getTaskBatchId, completeJobBatchDTO.getTaskBatchId()));

        JobTaskBatch jobTaskBatch = new JobTaskBatch();
        jobTaskBatch.setId(completeJobBatchDTO.getTaskBatchId());

        if (CollectionUtils.isEmpty(jobTasks)) {
            return false;
        }

        if (jobTasks.stream().anyMatch(jobTask -> JobTaskStatusEnum.NOT_COMPLETE.contains(jobTask.getTaskStatus()))) {
            return false;
        }

        Map<Integer, Long> statusCountMap = jobTasks.stream()
            .collect(Collectors.groupingBy(JobTask::getTaskStatus, Collectors.counting()));

        long failCount = statusCountMap.getOrDefault(JobTaskBatchStatusEnum.FAIL.getStatus(), 0L);
        long stopCount = statusCountMap.getOrDefault(JobTaskBatchStatusEnum.STOP.getStatus(), 0L);

        if (failCount > 0) {
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.FAIL.getStatus());
            SpringContext.CONTEXT.publishEvent(new JobTaskFailAlarmEvent(completeJobBatchDTO.getTaskBatchId()));
        } else if (stopCount > 0) {
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.STOP.getStatus());
        } else {
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.SUCCESS.getStatus());
        }

        if (Objects.nonNull(completeJobBatchDTO.getJobOperationReason())) {
            jobTaskBatch.setOperationReason(completeJobBatchDTO.getJobOperationReason());
        }

        if (Objects.nonNull(completeJobBatchDTO.getWorkflowNodeId()) && Objects.nonNull(completeJobBatchDTO.getWorkflowTaskBatchId())) {
            // 若是工作流则开启下一个任务
            try {
                WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
                taskExecuteDTO.setWorkflowTaskBatchId(completeJobBatchDTO.getWorkflowTaskBatchId());
                taskExecuteDTO.setTriggerType(JobTriggerTypeEnum.AUTO.getType());
                taskExecuteDTO.setParentId(completeJobBatchDTO.getWorkflowNodeId());
                // 这里取第一个的任务执行结果
                taskExecuteDTO.setResult(jobTasks.get(0).getResultMessage());
                ActorRef actorRef = ActorGenerator.workflowTaskExecutorActor();
                actorRef.tell(taskExecuteDTO, actorRef);
            } catch (Exception e) {
                log.error("任务调度执行失败", e);
            }
        }

        return 1 == jobTaskBatchMapper.update(jobTaskBatch,
                new LambdaUpdateWrapper<JobTaskBatch>()
                        .eq(JobTaskBatch::getId, completeJobBatchDTO.getTaskBatchId())
                        .in(JobTaskBatch::getTaskBatchStatus, JobTaskStatusEnum.NOT_COMPLETE)
        );

    }
}
