package com.aizuda.easy.retry.server.job.task.support.handler;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.enums.JobTriggerTypeEnum;
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

    /**
     * TODO 参数待优化
     *
     * @param workflowNodeId
     * @param workflowTaskBatchId
     * @param taskBatchId
     * @param jobOperationReason
     * @return
     */
    public boolean complete(Long workflowNodeId, Long workflowTaskBatchId, Long taskBatchId, Integer jobOperationReason) {

        List<JobTask> jobTasks = jobTaskMapper.selectList(
            new LambdaQueryWrapper<JobTask>().select(JobTask::getTaskStatus)
                .eq(JobTask::getTaskBatchId, taskBatchId));

        JobTaskBatch jobTaskBatch = new JobTaskBatch();
        jobTaskBatch.setId(taskBatchId);

        if (CollectionUtils.isEmpty(jobTasks)) {
            // todo 此为异常数据没有生成对应的任务项(task)， 直接更新为失败
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.FAIL.getStatus());
            jobTaskBatch.setOperationReason(JobOperationReasonEnum.NOT_EXECUTE_TASK.getReason());
            jobTaskBatchMapper.update(jobTaskBatch,
                new LambdaUpdateWrapper<JobTaskBatch>()
                    .eq(JobTaskBatch::getId, taskBatchId)
                    .in(JobTaskBatch::getTaskBatchStatus, JobTaskStatusEnum.NOT_COMPLETE));
            SpringContext.CONTEXT.publishEvent(new JobTaskFailAlarmEvent(taskBatchId));
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
            SpringContext.CONTEXT.publishEvent(new JobTaskFailAlarmEvent(taskBatchId));
        } else if (stopCount > 0) {
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.STOP.getStatus());
        } else {
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.SUCCESS.getStatus());
        }

        if (Objects.nonNull(jobOperationReason)) {
            jobTaskBatch.setOperationReason(jobOperationReason);
        }

        if (Objects.nonNull(workflowNodeId) && Objects.nonNull(workflowTaskBatchId)) {
            // 若是工作流则开启下一个任务
            try {
                WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
                taskExecuteDTO.setWorkflowTaskBatchId(workflowTaskBatchId);
                taskExecuteDTO.setTriggerType(JobTriggerTypeEnum.AUTO.getType());
                taskExecuteDTO.setParentId(workflowNodeId);
                ActorRef actorRef = ActorGenerator.workflowTaskExecutorActor();
                actorRef.tell(taskExecuteDTO, actorRef);
            } catch (Exception e) {
                log.error("任务调度执行失败", e);
            }
        }

        return 1 == jobTaskBatchMapper.update(jobTaskBatch,
                new LambdaUpdateWrapper<JobTaskBatch>()
                        .eq(JobTaskBatch::getId, taskBatchId)
                        .in(JobTaskBatch::getTaskBatchStatus, JobTaskStatusEnum.NOT_COMPLETE)
        );

    }
}
