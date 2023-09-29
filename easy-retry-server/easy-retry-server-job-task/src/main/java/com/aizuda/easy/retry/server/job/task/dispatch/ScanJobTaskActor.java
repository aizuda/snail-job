package com.aizuda.easy.retry.server.job.task.dispatch;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.cache.CacheConsumerGroup;
import com.aizuda.easy.retry.server.common.dto.PartitionTask;
import com.aizuda.easy.retry.server.common.dto.ScanTask;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.WaitStrategy;
import com.aizuda.easy.retry.server.job.task.dto.JobPartitionTask;
import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.aizuda.easy.retry.server.job.task.strategy.WaitStrategies.*;

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

    private static final AtomicLong lastId = new AtomicLong(0L);

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

        long total = process(startId -> listAvailableJobs(startId, scanTask), partitionTasks -> {
            for (final JobPartitionTask partitionTask : (List<JobPartitionTask>) partitionTasks) {
                CacheConsumerGroup.addOrUpdate(partitionTask.getGroupName());
                JobTaskPrepareDTO jobTaskPrepare = JobTaskConverter.INSTANCE.toJobTaskPrepare(partitionTask);

                // 更新下次触发时间
                WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(partitionTask.getTriggerType());
                WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
                waitStrategyContext.setTriggerType(partitionTask.getTriggerType());
                waitStrategyContext.setTriggerInterval(partitionTask.getTriggerInterval());
                waitStrategyContext.setNextTriggerAt(partitionTask.getNextTriggerAt());

                Job job = new Job();
                job.setId(partitionTask.getId());
                job.setNextTriggerAt(waitStrategy.computeRetryTime(waitStrategyContext));
                Assert.isTrue(1 == jobMapper.updateById(job),
                    () -> new EasyRetryServerException("更新job下次触发时间失败.jobId:[{}]", job.getId()));

                // 执行预处理阶段
                ActorRef actorRef = ActorGenerator.jobTaskPrepareActor();
                actorRef.tell(jobTaskPrepare, actorRef);
            }
        }, 0);

        log.info("job scan end. total:[{}]", total);
    }

    private List<JobPartitionTask> listAvailableJobs(Long startId, ScanTask scanTask) {

        List<Job> jobs = jobMapper.selectPage(new PageDTO<Job>(0, 1000),
            new LambdaQueryWrapper<Job>()
                .eq(Job::getJobStatus, StatusEnum.YES.getStatus())
                .in(Job::getBucketIndex, scanTask.getBuckets())
                .le(Job::getNextTriggerAt, LocalDateTime.now().plusSeconds(6))
                .eq(Job::getDeleted, StatusEnum.NO.getStatus())
                .gt(Job::getId, startId)
        ).getRecords();

        return JobTaskConverter.INSTANCE.toJobPartitionTasks(jobs);
    }

    public long process(
        Function<Long, List<? extends PartitionTask>> dataSource, Consumer<List<? extends PartitionTask>> task, long startId) {
        int total = 0;
        do {
            List<? extends PartitionTask> products = dataSource.apply(startId);
            if (CollectionUtils.isEmpty(products)) {
                // 没有查询到数据直接退出
                break;
            }

            total += products.size();

            task.accept(products);
            startId = maxId(products);
        } while (startId > 0);

        return total;
    }

    private static long maxId(List<? extends PartitionTask> products) {
        Optional<Long> max = products.stream().map(PartitionTask::getId).max(Long::compareTo);
        return max.orElse(-1L) + 1;
    }
}
