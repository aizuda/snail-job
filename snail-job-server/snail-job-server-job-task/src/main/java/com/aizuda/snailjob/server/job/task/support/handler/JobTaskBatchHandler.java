package com.aizuda.snailjob.server.job.task.support.handler;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.context.SpringContext;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.dto.DistributeInstance;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.CompleteJobBatchDTO;
import com.aizuda.snailjob.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.snailjob.server.job.task.dto.TaskExecuteDTO;
import com.aizuda.snailjob.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.snailjob.server.job.task.support.alarm.event.JobTaskFailAlarmEvent;
import com.aizuda.snailjob.server.job.task.support.cache.ResidentTaskCache;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.snailjob.server.job.task.support.timer.ResidentJobTimerTask;
import com.aizuda.snailjob.template.datasource.persistence.mapper.GroupConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: opensnail
 * @date : 2023-10-10 16:50
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobTaskBatchHandler {
    private final JobTaskMapper jobTaskMapper;
    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final WorkflowBatchHandler workflowBatchHandler;
    private final GroupConfigMapper groupConfigMapper;


    @Transactional
    public boolean complete(CompleteJobBatchDTO completeJobBatchDTO) {

        List<JobTask> jobTasks = jobTaskMapper.selectList(
                new LambdaQueryWrapper<JobTask>()
                        .select(JobTask::getTaskStatus, JobTask::getResultMessage)
                        .eq(JobTask::getTaskBatchId, completeJobBatchDTO.getTaskBatchId()));

        JobTaskBatch jobTaskBatch = new JobTaskBatch();
        jobTaskBatch.setId(completeJobBatchDTO.getTaskBatchId());

        if (CollUtil.isEmpty(jobTasks)) {
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
            SpringContext.getContext().publishEvent(new JobTaskFailAlarmEvent(completeJobBatchDTO.getTaskBatchId()));
        } else if (stopCount > 0) {
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.STOP.getStatus());
        } else {
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.SUCCESS.getStatus());
        }

        if (Objects.nonNull(completeJobBatchDTO.getJobOperationReason())) {
            jobTaskBatch.setOperationReason(completeJobBatchDTO.getJobOperationReason());
        }

        WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
        taskExecuteDTO.setWorkflowTaskBatchId(completeJobBatchDTO.getWorkflowTaskBatchId());
        taskExecuteDTO.setTaskExecutorScene(JobTaskExecutorSceneEnum.AUTO_WORKFLOW.getType());
        taskExecuteDTO.setParentId(completeJobBatchDTO.getWorkflowNodeId());
        taskExecuteDTO.setTaskBatchId(completeJobBatchDTO.getTaskBatchId());
        workflowBatchHandler.openNextNode(taskExecuteDTO);

        jobTaskBatch.setUpdateDt(LocalDateTime.now());
        return 1 == jobTaskBatchMapper.update(jobTaskBatch,
                new LambdaUpdateWrapper<JobTaskBatch>()
                        .eq(JobTaskBatch::getId, completeJobBatchDTO.getTaskBatchId())
                        .in(JobTaskBatch::getTaskBatchStatus, JobTaskBatchStatusEnum.NOT_COMPLETE)
        );

    }

    /**
     * 开启常驻任务
     *
     * @param job 定时任务配置信息
     * @param taskExecuteDTO 任务执行新
     */
    public void openResidentTask(Job job, TaskExecuteDTO taskExecuteDTO) {
        if (Objects.isNull(job)
                || JobTaskExecutorSceneEnum.MANUAL_JOB.getType().equals(taskExecuteDTO.getTaskExecutorScene())
                || JobTaskExecutorSceneEnum.AUTO_WORKFLOW.getType().equals(taskExecuteDTO.getTaskExecutorScene())
                || JobTaskExecutorSceneEnum.MANUAL_WORKFLOW.getType().equals(taskExecuteDTO.getTaskExecutorScene())
                // 是否是常驻任务
                || Objects.equals(StatusEnum.NO.getStatus(), job.getResident())
                // 防止任务已经分配到其他节点导致的任务重复执行
                || !DistributeInstance.INSTANCE.getConsumerBucket().contains(job.getBucketIndex())
        ) {
            return;
        }

        long count = groupConfigMapper.selectCount(new LambdaQueryWrapper<GroupConfig>()
                .eq(GroupConfig::getNamespaceId, job.getNamespaceId())
                .eq(GroupConfig::getGroupName, job.getGroupName())
                .eq(GroupConfig::getGroupStatus, StatusEnum.YES.getStatus()));
        if (count == 0) {
            return;
        }

        JobTimerTaskDTO jobTimerTaskDTO = new JobTimerTaskDTO();
        jobTimerTaskDTO.setJobId(taskExecuteDTO.getJobId());
        jobTimerTaskDTO.setTaskBatchId(taskExecuteDTO.getTaskBatchId());
        jobTimerTaskDTO.setTaskExecutorScene(JobTaskExecutorSceneEnum.AUTO_JOB.getType());
        WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(job.getTriggerType());

        Long preTriggerAt = ResidentTaskCache.get(job.getId());
        if (Objects.isNull(preTriggerAt) || preTriggerAt < job.getNextTriggerAt()) {
            preTriggerAt = job.getNextTriggerAt();
        }

        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
        waitStrategyContext.setTriggerInterval(job.getTriggerInterval());
        waitStrategyContext.setNextTriggerAt(preTriggerAt);
        Long nextTriggerAt = waitStrategy.computeTriggerTime(waitStrategyContext);

        // 获取时间差的毫秒数
        long milliseconds = nextTriggerAt - preTriggerAt;

        Duration duration = Duration.ofMillis(milliseconds - DateUtils.toNowMilli() % 1000);

        log.info("常驻任务监控. [{}] 任务时间差:[{}] 取余:[{}]", duration, milliseconds, DateUtils.toNowMilli() % 1000);
        job.setNextTriggerAt(nextTriggerAt);
        JobTimerWheel.registerWithJob(() -> new ResidentJobTimerTask(jobTimerTaskDTO, job), duration);
        ResidentTaskCache.refresh(job.getId(), nextTriggerAt);
    }
}
