package com.aizuda.snailjob.server.job.task.support.handler;

import akka.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.dto.DistributeInstance;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.*;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
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

import static com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum.COMPLETED;
import static com.aizuda.snailjob.common.core.enums.MapReduceStageEnum.*;

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

        // 幂等处理
        Long countJobTaskBatch = jobTaskBatchMapper.selectCount(new LambdaQueryWrapper<JobTaskBatch>()
                .eq(JobTaskBatch::getId, completeJobBatchDTO.getTaskBatchId())
                .in(JobTaskBatch::getTaskBatchStatus, COMPLETED)
        );
        if (countJobTaskBatch > 0) {
            // 批次已经完成了，不需要重复更新
            return true;
        }

        List<JobTask> jobTasks = jobTaskMapper.selectList(
                new LambdaQueryWrapper<JobTask>()
                        .select(JobTask::getTaskStatus, JobTask::getMrStage)
                        .eq(JobTask::getTaskBatchId, completeJobBatchDTO.getTaskBatchId()));

        if (CollUtil.isEmpty(jobTasks) ||
                jobTasks.stream().anyMatch(jobTask -> JobTaskStatusEnum.NOT_COMPLETE.contains(jobTask.getTaskStatus()))) {
            return false;
        }

        JobTaskBatch jobTaskBatch = new JobTaskBatch();
        jobTaskBatch.setId(completeJobBatchDTO.getTaskBatchId());

        Map<Integer, Long> statusCountMap = jobTasks.stream()
                .collect(Collectors.groupingBy(JobTask::getTaskStatus, Collectors.counting()));

        long failCount = statusCountMap.getOrDefault(JobTaskBatchStatusEnum.FAIL.getStatus(), 0L);
        long stopCount = statusCountMap.getOrDefault(JobTaskBatchStatusEnum.STOP.getStatus(), 0L);

        if (failCount > 0) {
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.FAIL.getStatus());
            SnailSpringContext.getContext().publishEvent(new JobTaskFailAlarmEvent(completeJobBatchDTO.getTaskBatchId()));
        } else if (stopCount > 0) {
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.STOP.getStatus());
        } else {

            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.SUCCESS.getStatus());
            if (needReduceTask(completeJobBatchDTO, jobTasks)
                    && JobTaskTypeEnum.MAP_REDUCE.getType() == completeJobBatchDTO.getTaskType()) {
                // 此时中断批次完成，需要开启reduce任务
                return false;
            }
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
     * 若需要执行reduce则返回false 不需要更新批次状态， 否则需要更新批次状态
     *
     * @param completeJobBatchDTO 需要执行批次完成所需的参数信息
     * @param jobTasks            任务项列表
     * @return true-需要reduce false-不需要reduce
     */
    private boolean needReduceTask(final CompleteJobBatchDTO completeJobBatchDTO, final List<JobTask> jobTasks) {
        Integer mrStage = null;

        int reduceCount = 0;
        int mapCount = 0;
        for (final JobTask jobTask : jobTasks) {
            if (Objects.isNull(jobTask.getMrStage())) {
                continue;
            }

            // 存在MERGE_REDUCE任务了不需要生成
            if (MERGE_REDUCE.getStage() == jobTask.getMrStage()) {
                return false;
            }

            // REDUCE任务累加
            if (REDUCE.getStage() == jobTask.getMrStage()) {
                reduceCount++;
                continue;
            }

            // MAP任务累加
            if (MAP.getStage() == jobTask.getMrStage()) {
                mapCount++;
            }
        }

        // 若存在2个以上的reduce任务则开启merge reduce任务
        if (reduceCount > 1) {
            mrStage = MERGE_REDUCE.getStage();
        } else if (mapCount == jobTasks.size()) {
            // 若都是MAP任务则开启Reduce任务
            mrStage = REDUCE.getStage();
        } else {
            // 若既不是MAP也是不REDUCE则是其他类型的任务，直接返回即可
            return false;
        }

        // 开启reduce or mergeReduce阶段
        try {
            ReduceTaskDTO reduceTaskDTO = JobTaskConverter.INSTANCE.toReduceTaskDTO(completeJobBatchDTO);
            reduceTaskDTO.setMrStage(mrStage);
            ActorRef actorRef = ActorGenerator.jobReduceActor();
            actorRef.tell(reduceTaskDTO, actorRef);
            return true;
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("tell reduce actor error", e);
        }

        return false;
    }

    private static boolean isAllMapTask(final List<JobTask> jobTasks) {
        return jobTasks.size() == jobTasks.stream()
                .filter(jobTask -> Objects.nonNull(jobTask.getMrStage()) && MAP.getStage() == jobTask.getMrStage())
                .count();
    }

    private static boolean isALeastOneReduceTask(final List<JobTask> jobTasks) {
        return jobTasks.stream()
                .filter(
                        jobTask -> Objects.nonNull(jobTask.getMrStage()) && REDUCE.getStage() == jobTask.getMrStage())
                .count() > 1;
    }

    /**
     * 开启常驻任务
     *
     * @param job            定时任务配置信息
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

        log.debug("常驻任务监控. [{}] 任务时间差:[{}] 取余:[{}]", duration, milliseconds,
                DateUtils.toNowMilli() % 1000);
        job.setNextTriggerAt(nextTriggerAt);
        JobTimerWheel.registerWithJob(() -> new ResidentJobTimerTask(jobTimerTaskDTO, job), duration);
        ResidentTaskCache.refresh(job.getId(), nextTriggerAt);
    }
}
