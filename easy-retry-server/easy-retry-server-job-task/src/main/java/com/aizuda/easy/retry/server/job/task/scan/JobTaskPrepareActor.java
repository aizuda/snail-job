package com.aizuda.easy.retry.server.job.task.scan;

import akka.actor.AbstractActor;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.easy.retry.server.job.task.BlockStrategy;
import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.enums.TaskStatusEnum;
import com.aizuda.easy.retry.server.job.task.strategy.BlockStrategies;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 调度任务准备阶段
 *
 * @author www.byteblogs.com
 * @date 2023-09-25 22:20:53
 * @since
 */
@Component(ActorGenerator.SCAN_JOB_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class JobTaskPrepareActor extends AbstractActor {

    @Autowired
    private JobMapper jobMapper;
    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    protected ClientNodeAllocateHandler clientNodeAllocateHandler;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(JobTaskPrepareDTO.class, job -> {
            try {
                doPrepare(job);
            } catch (Exception e) {

            }
        }).build();
    }

    private void doPrepare(JobTaskPrepareDTO prepare) {

        JobTask jobTask = jobTaskMapper.selectOne(new LambdaQueryWrapper<JobTask>()
            .eq(JobTask::getJobId, prepare.getJobId())
            .in(JobTask::getTaskStatus,
                TaskStatusEnum.WAIT.getStatus(), TaskStatusEnum.INTERRUPTING.getStatus(),
                TaskStatusEnum.INTERRUPTING.getStatus()));
        if (Objects.isNull(jobTask)) {
            // 生成可执行任务
            RegisterNodeInfo serverNode = clientNodeAllocateHandler.getServerNode(prepare.getGroupName());
            if (Objects.isNull(serverNode)) {
                log.error("无可执行的客户端信息. jobId:[{}]", prepare.getJobId());
                return;
            }

            jobTask = new JobTask();
            jobTask.setHostId(serverNode.getHostId());
            jobTask.setJobId(prepare.getJobId());
            jobTask.setGroupName(prepare.getGroupName());
            Assert.isTrue(1 == jobTaskMapper.insert(jobTask),
                () -> new EasyRetryServerException("新增调度任务失败.jobId:[{}]", prepare.getJobId()));

            // 进入时间轮
            JobContext jobContext = new JobContext();
            jobContext.setRegisterNodeInfo(serverNode);
            long delay = prepare.getNextTriggerAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                - System.currentTimeMillis();
            JobTimerWheelHandler.register(prepare.getGroupName(), prepare.getJobId().toString(),
                new JobTimerTask(jobTask.getId(), jobTask.getGroupName()), delay, TimeUnit.MILLISECONDS);
        } else {

            BlockStrategies.BlockStrategyContext blockStrategyContext = new BlockStrategies.BlockStrategyContext();
            blockStrategyContext.setRegisterNodeInfo(CacheRegisterTable.getServerNode(jobTask.getGroupName(), jobTask.getHostId()));
            BlockStrategy blockStrategy = BlockStrategies.BlockStrategyEnum.getBlockStrategy(
                prepare.getBlockStrategy());
            blockStrategy.block(blockStrategyContext);
        }


    }
}
