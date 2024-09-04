package com.aizuda.snailjob.server.job.task.support.handler;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.dto.DistributeInstance;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.*;
import com.aizuda.snailjob.server.job.task.support.JobExecutorResultHandler;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.cache.ResidentTaskCache;
import com.aizuda.snailjob.server.job.task.support.result.job.JobExecutorResultContext;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.snailjob.server.job.task.support.timer.ResidentJobTimerTask;
import com.aizuda.snailjob.template.datasource.persistence.mapper.GroupConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum.COMPLETED;

/**
 * @author: opensnail
 * @date : 2023-10-10 16:50
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobTaskBatchHandler {

    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final GroupConfigMapper groupConfigMapper;
    private final List<JobExecutorResultHandler> resultHandlerList;

    @Transactional
    public boolean handleResult(CompleteJobBatchDTO completeJobBatchDTO) {
        Assert.notNull(completeJobBatchDTO.getTaskType(), ()-> new SnailJobServerException("taskType can not be null"));

        // 非重试流量幂等处理
        if(!completeJobBatchDTO.isRetry()) {
            // 幂等处理
            Long countJobTaskBatch = jobTaskBatchMapper.selectCount(new LambdaQueryWrapper<JobTaskBatch>()
                .eq(JobTaskBatch::getId, completeJobBatchDTO.getTaskBatchId())
                .in(JobTaskBatch::getTaskBatchStatus, COMPLETED)
            );
            if (countJobTaskBatch > 0) {
                // 批次已经完成了，不需要重复更新
                return true;
            }
        }

        JobExecutorResultContext context = JobTaskConverter.INSTANCE.toJobExecutorResultContext(completeJobBatchDTO);
        for (final JobExecutorResultHandler jobExecutorResultHandler : resultHandlerList) {
            if (completeJobBatchDTO.getTaskType().equals(jobExecutorResultHandler.getTaskInstanceType().getType())) {
                jobExecutorResultHandler.handleResult(context);
                break;
            }
        }

        // 处理的结果 若已经更新成功 或者 需要开启reduce任务都算是已经处理了
        return context.isTaskBatchComplete() || context.isCreateReduceTask();
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
