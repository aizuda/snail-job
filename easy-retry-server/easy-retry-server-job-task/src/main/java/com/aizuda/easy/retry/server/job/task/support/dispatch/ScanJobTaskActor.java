package com.aizuda.easy.retry.server.job.task.support.dispatch;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.WaitStrategy;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.cache.CacheConsumerGroup;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.dto.PartitionTask;
import com.aizuda.easy.retry.server.common.dto.ScanTask;
import com.aizuda.easy.retry.server.common.enums.JobTriggerTypeEnum;
import com.aizuda.easy.retry.server.common.strategy.WaitStrategies;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.common.util.PartitionTaskUtils;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.dto.JobPartitionTask;
import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.support.cache.ResidentTaskCache;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;


/**
 * JOB任务扫描
 *
 * @author: www.byteblogs.com
 * @date : 2023-09-22 09:08
 * @since 2.4.0
 */
@Component(ActorGenerator.SCAN_JOB_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ScanJobTaskActor extends AbstractActor {

    @Autowired
    private JobMapper jobMapper;
    @Autowired
    private SystemProperties systemProperties;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ScanTask.class, config -> {

            try {
                doScan(config);
            } catch (Exception e) {
                LogUtils.error(log, "Data scanner processing exception. [{}]", config, e);
            }

        }).build();

    }

    private void doScan(final ScanTask scanTask) {
        if (CollectionUtils.isEmpty(scanTask.getBuckets())) {
            return;
        }

        long total = PartitionTaskUtils.process(startId -> listAvailableJobs(startId, scanTask),
                this::processJobPartitionTasks, 0);

        log.info("job scan end. total:[{}]", total);
    }

    private void processJobPartitionTasks(List<? extends PartitionTask> partitionTasks) {

        List<Job> waitUpdateJobs = new ArrayList<>();
        List<JobTaskPrepareDTO> waitExecJobs = new ArrayList<>();
        long now = DateUtils.toNowMilli();
        for (PartitionTask partitionTask : partitionTasks) {
            processJob((JobPartitionTask) partitionTask, waitUpdateJobs, waitExecJobs, now);
        }

        // 批量更新
        jobMapper.updateBatchNextTriggerAtById(waitUpdateJobs);

        for (final JobTaskPrepareDTO waitExecJob : waitExecJobs) {
            // 执行预处理阶段
            ActorRef actorRef = ActorGenerator.jobTaskPrepareActor();
            waitExecJob.setTriggerType(JobTriggerTypeEnum.AUTO.getType());
            actorRef.tell(waitExecJob, actorRef);
        }
    }

    private void processJob(JobPartitionTask partitionTask, final List<Job> waitUpdateJobs,
                            final List<JobTaskPrepareDTO> waitExecJobs, long now) {
        CacheConsumerGroup.addOrUpdate(partitionTask.getGroupName(), partitionTask.getNamespaceId());

        Job job = new Job();
        job.setId(partitionTask.getId());

        boolean triggerTask = true;
        Long nextTriggerAt = ResidentTaskCache.get(partitionTask.getId());
        if (needCalculateNextTriggerTime(partitionTask)) {
            // 更新下次触发时间
            nextTriggerAt = calculateNextTriggerTime(partitionTask, now);
        } else {
            // 若常驻任务的缓存时间为空则触发一次任务调度，说明常驻任务长时间未更新或者是系统刚刚启动
            triggerTask = Objects.isNull(nextTriggerAt);
            // 若出现常驻任务时间为null或者常驻任务的内存时间长期未更新, 刷新为now
            if (Objects.isNull(nextTriggerAt)
                    || (nextTriggerAt + DateUtils.toEpochMilli(SystemConstants.SCHEDULE_PERIOD)) < now) {
                nextTriggerAt = now;
            }
        }

        job.setNextTriggerAt(nextTriggerAt);

        waitUpdateJobs.add(job);

        if (triggerTask) {
            waitExecJobs.add(JobTaskConverter.INSTANCE.toJobTaskPrepare(partitionTask));
        }

    }

    /**
     * 需要重新计算触发时间的条件 1、不是常驻任务 2、常驻任务缓存的触发任务为空 3、常驻任务中的触发时间不是最新的
     */
    private static boolean needCalculateNextTriggerTime(JobPartitionTask partitionTask) {
        return !Objects.equals(StatusEnum.YES.getStatus(), partitionTask.getResident());
    }

    private Long calculateNextTriggerTime(JobPartitionTask partitionTask, long now) {

        long nextTriggerAt = partitionTask.getNextTriggerAt();
        if ((nextTriggerAt + DateUtils.toEpochMilli(SystemConstants.SCHEDULE_PERIOD)) < now) {
            nextTriggerAt = now;
            partitionTask.setNextTriggerAt(nextTriggerAt);
        }

        // 更新下次触发时间
        WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(partitionTask.getTriggerType());
        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
        waitStrategyContext.setTriggerInterval(partitionTask.getTriggerInterval());
        waitStrategyContext.setNextTriggerAt(nextTriggerAt);

        return waitStrategy.computeTriggerTime(waitStrategyContext);
    }

    private List<JobPartitionTask> listAvailableJobs(Long startId, ScanTask scanTask) {
        if (CollectionUtils.isEmpty(scanTask.getBuckets())) {
            return Collections.emptyList();
        }

        List<Job> jobs = jobMapper.selectPage(new PageDTO<>(0, systemProperties.getJobPullPageSize()),
                new LambdaQueryWrapper<Job>()
                        .select(Job::getGroupName, Job::getNextTriggerAt, Job::getBlockStrategy, Job::getTriggerType,
                                Job::getTriggerInterval, Job::getExecutorTimeout, Job::getTaskType, Job::getResident,
                                Job::getId, Job::getNamespaceId)
                        .eq(Job::getJobStatus, StatusEnum.YES.getStatus())
                        .eq(Job::getDeleted, StatusEnum.NO.getStatus())
                        .in(Job::getBucketIndex, scanTask.getBuckets())
                        .le(Job::getNextTriggerAt, DateUtils.toNowMilli() + DateUtils.toEpochMilli(SystemConstants.SCHEDULE_PERIOD))
                        .ge(Job::getId, startId)
                        .orderByAsc(Job::getId)
        ).getRecords();

        return JobTaskConverter.INSTANCE.toJobPartitionTasks(jobs);
    }
}
