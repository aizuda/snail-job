package com.aizuda.easy.retry.server.job.task.scan;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.common.dto.ScanTask;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.easy.retry.server.job.task.BlockStrategy;
import com.aizuda.easy.retry.server.job.task.WaitStrategy;
import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.strategy.BlockStrategies;
import com.aizuda.easy.retry.server.job.task.strategy.BlockStrategies.BlockStrategyContext;
import com.aizuda.easy.retry.server.job.task.strategy.BlockStrategies.BlockStrategyEnum;
import com.aizuda.easy.retry.server.job.task.strategy.WaitStrategies;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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
    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    protected ClientNodeAllocateHandler clientNodeAllocateHandler;

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

        List<Job> jobs = listAvailableJobs(lastId.get());
        if (CollectionUtils.isEmpty(jobs)) {
            // 数据为空则休眠5s
            try {
                Thread.sleep((10 / 2) * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 重置最大id
            lastId.set(0L);
            return;
        }

        lastId.set(jobs.get(jobs.size() - 1).getId());

        for (Job job : jobs) {

            // 更新下次触发时间
            WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(job.getTriggerType());
            WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
            waitStrategyContext.setTriggerType(job.getTriggerType());
            waitStrategyContext.setTriggerInterval(job.getTriggerInterval());
            waitStrategyContext.setNextTriggerAt(job.getNextTriggerAt());
            job.setNextTriggerAt(waitStrategy.computeRetryTime(waitStrategyContext));
            Assert.isTrue(1 == jobMapper.updateById(job), () -> new EasyRetryServerException("更新job下次触发时间失败.jobId:[{}]", job.getId()));

            // 执行预处理阶段
            ActorRef actorRef = ActorGenerator.jobTaskPrepareActor();
            JobTaskPrepareDTO jobTaskPrepareDTO = new JobTaskPrepareDTO();
            jobTaskPrepareDTO.setJobId(job.getId());
            jobTaskPrepareDTO.setTriggerType(job.getTriggerType());
            jobTaskPrepareDTO.setNextTriggerAt(job.getNextTriggerAt());
            actorRef.tell(jobTaskPrepareDTO, actorRef);
        }

    }

    private List<Job> listAvailableJobs(Long lastId) {
        return jobMapper.selectPage(new PageDTO<Job>(0, 1000),
                new LambdaQueryWrapper<Job>()
                        .eq(Job::getJobStatus, StatusEnum.YES.getStatus())
                        // TODO 提前10秒把需要执行的任务拉取出来
                        .le(Job::getNextTriggerAt, LocalDateTime.now().plusSeconds(10))
                        .eq(Job::getDeleted, StatusEnum.NO.getStatus())
                        .gt(Job::getId, lastId)
        ).getRecords();
    }
}
