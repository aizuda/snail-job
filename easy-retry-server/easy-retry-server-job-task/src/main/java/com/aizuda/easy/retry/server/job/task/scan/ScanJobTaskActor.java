package com.aizuda.easy.retry.server.job.task.scan;

import akka.actor.AbstractActor;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.common.dto.ScanTask;
import com.aizuda.easy.retry.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.easy.retry.server.job.task.BlockStrategy;
import com.aizuda.easy.retry.server.job.task.strategy.BlockStrategies;
import com.aizuda.easy.retry.server.job.task.strategy.BlockStrategies.BlockStrategyContext;
import com.aizuda.easy.retry.server.job.task.strategy.BlockStrategies.BlockStrategyEnum;
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
            JobContext jobContext = new JobContext();
            // 选择客户端节点
            // TODO 校验一下客户端是否存活
            jobContext.setRegisterNodeInfo(clientNodeAllocateHandler.getServerNode(job.getGroupName()));

            Long count = jobTaskMapper.selectCount(new LambdaQueryWrapper<JobTask>().eq(JobTask::getTaskStatus, StatusEnum.YES.getStatus()));
            if (count <= 0) {
                // 生成可执行任务
                JobTask jobTask = new JobTask();
                jobTask.setJobId(job.getId());
                jobTask.setGroupName(job.getGroupName());
                jobTaskMapper.insert(jobTask);

                // 更新下次触发时间
                // ToDo 根据CRON表达式计算
                job.setNextTriggerAt(LocalDateTime.now().plusSeconds(50));
                jobMapper.updateById(job);
            } else {
                BlockStrategyContext blockStrategyContext = new BlockStrategyContext();

                BlockStrategy blockStrategy = BlockStrategyEnum.getBlockStrategy(job.getBlockStrategy());
                blockStrategy.block(blockStrategyContext);
            }



            // 进入时间轮
            JobTimerWheelHandler.register(job.getGroupName(), job.getId().toString(), new JobTimerTask(), 1, TimeUnit.MILLISECONDS);
            // 校验是否存在已经在执行的任务了
//            boolean isExist = true;
//            if (isExist) {
//                // 选择丢弃策略
//               String blockStrategy = job.getBlockStrategy();
//
//            }
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
