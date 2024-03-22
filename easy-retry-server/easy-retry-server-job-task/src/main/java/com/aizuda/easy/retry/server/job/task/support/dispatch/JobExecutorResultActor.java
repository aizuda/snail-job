package com.aizuda.easy.retry.server.job.task.support.dispatch;

import akka.actor.AbstractActor;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.enums.JobTaskTypeEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.dto.CompleteJobBatchDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskStopHandler;
import com.aizuda.easy.retry.server.job.task.support.handler.DistributedLockHandler;
import com.aizuda.easy.retry.server.job.task.support.handler.JobTaskBatchHandler;
import com.aizuda.easy.retry.server.job.task.support.stop.JobTaskStopFactory;
import com.aizuda.easy.retry.server.job.task.support.stop.TaskStopJobContext;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.Objects;

/**
 * @author www.byteblogs.com
 * @date 2023-10-05 17:16:35
 * @since 2.4.0
 */
@Component(ActorGenerator.JOB_EXECUTOR_RESULT_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class JobExecutorResultActor extends AbstractActor {
    private static final String KEY = "job_complete_{0}_{1}";
    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    private JobTaskBatchHandler jobTaskBatchHandler;
    @Autowired
    private DistributedLockHandler distributedLockHandler;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(JobExecutorResultDTO.class, result -> {
            log.debug("更新任务状态. 参数:[{}]", JsonUtil.toJsonString(result));
            try {
                JobTask jobTask = new JobTask();
                jobTask.setTaskStatus(result.getTaskStatus());
                if (Objects.nonNull(result.getResult())) {
                    jobTask.setResultMessage(JsonUtil.toJsonString(result.getResult()));
                }

                Assert.isTrue(1 == jobTaskMapper.update(jobTask,
                        new LambdaUpdateWrapper<JobTask>().eq(JobTask::getId, result.getTaskId())),
                    () -> new EasyRetryServerException("更新任务实例失败"));
                // 先尝试完成，若已完成则不需要通过获取分布式锁来完成
                boolean tryCompleteAndStop = tryCompleteAndStop(result);
                 if (!tryCompleteAndStop) {
                    // 存在并发问题
                    distributedLockHandler.lockWithDisposableAndRetry(() -> {
                        tryCompleteAndStop(result);
                    }, MessageFormat.format(KEY, result.getTaskBatchId(),
                        result.getJobId()), Duration.ofSeconds(1), Duration.ofSeconds(1), 3);
                }
            } catch (Exception e) {
                EasyRetryLog.LOCAL.error(" job executor result exception. [{}]", result, e);
            } finally {
                getContext().stop(getSelf());
            }

        }).build();

    }

    private boolean tryCompleteAndStop(JobExecutorResultDTO result) {
        CompleteJobBatchDTO completeJobBatchDTO = JobTaskConverter.INSTANCE.toCompleteJobBatchDTO(result);
        boolean complete = jobTaskBatchHandler.complete(completeJobBatchDTO);
        if (complete) {
            // 尝试停止任务
            // 若是集群任务则客户端会主动关闭
            if (result.getTaskType() != JobTaskTypeEnum.CLUSTER.getType()) {
                JobTaskStopHandler instanceInterrupt = JobTaskStopFactory.getJobTaskStop(result.getTaskType());
                TaskStopJobContext stopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(result);
                stopJobContext.setNeedUpdateTaskStatus(Boolean.FALSE);
                stopJobContext.setForceStop(Boolean.TRUE);
                instanceInterrupt.stop(stopJobContext);
            }
        }

        return complete;
    }
}
