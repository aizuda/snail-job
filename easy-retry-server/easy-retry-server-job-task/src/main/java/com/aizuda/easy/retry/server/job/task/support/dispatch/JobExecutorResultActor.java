package com.aizuda.easy.retry.server.job.task.support.dispatch;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.enums.TaskTypeEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.easy.retry.server.job.task.dto.JobLogDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskStopHandler;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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

    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private JobTaskBatchHandler jobTaskBatchHandler;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(JobExecutorResultDTO.class, result -> {
            log.info("更新任务状态. 参数:[{}]", JsonUtil.toJsonString(result));
            try {
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(final TransactionStatus status) {
                        JobTask jobTask = new JobTask();
                        jobTask.setTaskStatus(result.getTaskStatus());
                        if (Objects.nonNull(result.getResult())) {
                            jobTask.setResultMessage(JsonUtil.toJsonString(result.getResult()));
                        }

                        Assert.isTrue(1 == jobTaskMapper.update(jobTask,
                                        new LambdaUpdateWrapper<JobTask>().eq(JobTask::getId, result.getTaskId())),
                                ()-> new EasyRetryServerException("更新任务实例失败"));

                        // 更新批次上的状态
                        boolean complete = jobTaskBatchHandler.complete(result.getWorkflowNodeId(), result.getWorkflowTaskBatchId(), result.getTaskBatchId(), result.getJobOperationReason());
                        if (complete) {
                            // 尝试停止任务
                            // 若是集群任务则客户端会主动关闭
                            if (result.getTaskType() != TaskTypeEnum.CLUSTER.getType()) {
                                JobTaskStopHandler instanceInterrupt = JobTaskStopFactory.getJobTaskStop(result.getTaskType());
                                TaskStopJobContext stopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(result);
                                stopJobContext.setNeedUpdateTaskStatus(Boolean.FALSE);
                                stopJobContext.setForceStop(Boolean.TRUE);
                                instanceInterrupt.stop(stopJobContext);
                            }
                        }
                    }
                });

                JobLogDTO jobLogDTO = JobTaskConverter.INSTANCE.toJobLogDTO(result);
                jobLogDTO.setMessage(result.getMessage());
                jobLogDTO.setTaskId(result.getTaskId());
                ActorRef actorRef = ActorGenerator.jobLogActor();
                actorRef.tell(jobLogDTO, actorRef);
            } catch (Exception e) {
                LogUtils.error(log, " job executor result exception. [{}]", result, e);
            } finally {
                getContext().stop(getSelf());
            }

        }).build();
    }
}
