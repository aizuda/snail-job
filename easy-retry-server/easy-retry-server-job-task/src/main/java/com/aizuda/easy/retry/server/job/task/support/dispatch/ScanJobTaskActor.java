package com.aizuda.easy.retry.server.job.task.support.dispatch;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.cache.CacheConsumerGroup;
import com.aizuda.easy.retry.server.common.dto.PartitionTask;
import com.aizuda.easy.retry.server.common.dto.ScanTask;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.util.PartitionTaskUtils;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.dto.JobPartitionTask;
import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.support.WaitStrategy;
import com.aizuda.easy.retry.server.job.task.support.cache.ResidentTaskCache;
import com.aizuda.easy.retry.server.job.task.support.timer.JobTimerTask;
import com.aizuda.easy.retry.server.job.task.support.timer.ResidentJobTimerTask;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.aizuda.easy.retry.server.job.task.support.strategy.WaitStrategies.*;

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
        log.info("job scan start");
        if (CollectionUtils.isEmpty(scanTask.getBuckets())) {
            return;
        }

        long total = PartitionTaskUtils.process(startId -> listAvailableJobs(startId, scanTask), this::processJobPartitionTasks, scanTask.getStartId());

        log.info("job scan end. total:[{}]", total);
    }

    private void processJobPartitionTasks(List<? extends PartitionTask> partitionTasks) {
        for (PartitionTask partitionTask : partitionTasks) {
            processJob((JobPartitionTask) partitionTask);
        }
    }

    private void processJob(JobPartitionTask partitionTask) {
        CacheConsumerGroup.addOrUpdate(partitionTask.getGroupName());
        JobTaskPrepareDTO jobTaskPrepare = JobTaskConverter.INSTANCE.toJobTaskPrepare(partitionTask);

        Job job = new Job();
        job.setId(partitionTask.getId());

        boolean triggerTask = true;
        LocalDateTime nextTriggerAt = ResidentTaskCache.get(partitionTask.getId());
        if (needCalculateNextTriggerTime(partitionTask, nextTriggerAt)) {
            // 更新下次触发时间
           nextTriggerAt = calculateNextTriggerTime(partitionTask);
        } else {
            // 若常驻任务的缓存时间为空则触发一次任务调度，说明常驻任务长时间未更新或者是系统刚刚启动
            triggerTask = Objects.isNull(nextTriggerAt);
            // 若出现常驻任务时间为null或者常驻任务的内存时间长期未更新, 刷新为now
            LocalDateTime now = LocalDateTime.now();
            if (Objects.isNull(nextTriggerAt) || nextTriggerAt.plusSeconds(SystemConstants.SCHEDULE_PERIOD).isBefore(now)) {
                nextTriggerAt = now;
            }
        }

        job.setNextTriggerAt(nextTriggerAt);
        Assert.isTrue(1 == jobMapper.updateById(job),
                () -> new EasyRetryServerException("更新job下次触发时间失败.jobId:[{}]", job.getId()));

        if (triggerTask) {
            // 执行预处理阶段
            ActorRef actorRef = ActorGenerator.jobTaskPrepareActor();
            actorRef.tell(jobTaskPrepare, actorRef);
        }
    }

    /**
     * 需要重新计算触发时间的条件
     * 1、不是常驻任务
     * 2、常驻任务缓存的触发任务为空
     * 3、常驻任务中的触发时间不是最新的
     */
    private static boolean needCalculateNextTriggerTime(JobPartitionTask partitionTask, LocalDateTime nextTriggerAt) {
        return !Objects.equals(StatusEnum.YES.getStatus(), partitionTask.getResident());
    }

    private LocalDateTime calculateNextTriggerTime(JobPartitionTask partitionTask) {
        // 更新下次触发时间
        WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(partitionTask.getTriggerType());
        WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
        waitStrategyContext.setTriggerType(partitionTask.getTriggerType());
        waitStrategyContext.setTriggerInterval(partitionTask.getTriggerInterval());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextTriggerAt = partitionTask.getNextTriggerAt();
        if (nextTriggerAt.plusSeconds(SystemConstants.SCHEDULE_PERIOD).isBefore(now)) {
            nextTriggerAt = now;
        }

        waitStrategyContext.setNextTriggerAt(nextTriggerAt);

        return waitStrategy.computeRetryTime(waitStrategyContext);
    }

    private List<JobPartitionTask> listAvailableJobs(Long startId, ScanTask scanTask) {
        if (CollectionUtils.isEmpty(scanTask.getBuckets())) {
            return Collections.emptyList();
        }

        List<Job> jobs = jobMapper.selectPage(new PageDTO<Job>(0, scanTask.getSize()),
                new LambdaQueryWrapper<Job>()
                        .eq(Job::getJobStatus, StatusEnum.YES.getStatus())
                        .in(Job::getBucketIndex, scanTask.getBuckets())
                        .le(Job::getNextTriggerAt, LocalDateTime.now().plusSeconds(SystemConstants.SCHEDULE_PERIOD))
                        .eq(Job::getDeleted, StatusEnum.NO.getStatus())
                        .ge(Job::getId, startId)
        ).getRecords();

        return JobTaskConverter.INSTANCE.toJobPartitionTasks(jobs);
    }
}
