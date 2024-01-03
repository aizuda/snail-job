package com.aizuda.easy.retry.server.job.task.support.dispatch;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.WaitStrategy;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.common.enums.JobExecuteStrategyEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.strategy.WaitStrategies;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.easy.retry.server.job.task.dto.TaskExecuteDTO;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.easy.retry.server.job.task.support.JobExecutor;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.cache.ResidentTaskCache;
import com.aizuda.easy.retry.server.job.task.support.event.JobTaskFailAlarmEvent;
import com.aizuda.easy.retry.server.job.task.support.executor.job.JobExecutorContext;
import com.aizuda.easy.retry.server.job.task.support.executor.job.JobExecutorFactory;
import com.aizuda.easy.retry.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.easy.retry.server.job.task.support.timer.ResidentJobTimerTask;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-25 17:41
 * @since : 2.4.0
 */
@Component(ActorGenerator.JOB_EXECUTOR_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class JobExecutorActor extends AbstractActor {
    @Autowired
    private JobMapper jobMapper;
    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(TaskExecuteDTO.class, taskExecute -> {
            try {
                log.info("准备执行任务. [{}] [{}]", LocalDateTime.now(), JsonUtil.toJsonString(taskExecute));
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(final TransactionStatus status) {
                        doExecute(taskExecute);
                    }
                });

            } catch (Exception e) {
                LogUtils.error(log, "job executor exception. [{}]", taskExecute, e);
                handlerTaskBatch(taskExecute, JobTaskBatchStatusEnum.FAIL.getStatus(), JobOperationReasonEnum.TASK_EXECUTE_ERROR.getReason());
                SpringContext.CONTEXT.publishEvent(new JobTaskFailAlarmEvent(taskExecute.getTaskBatchId()));
            } finally {
                getContext().stop(getSelf());
            }
        }).build();
    }

    private void doExecute(final TaskExecuteDTO taskExecute) {

        LambdaQueryWrapper<Job> queryWrapper = new LambdaQueryWrapper<>();
        // 自动地校验任务必须是开启状态，手动触发无需校验
        if (JobExecuteStrategyEnum.AUTO.getType().equals(taskExecute.getTriggerType())) {
            queryWrapper.eq(Job::getJobStatus, StatusEnum.YES.getStatus());
        }

        Job job = jobMapper.selectOne(queryWrapper.eq(Job::getId, taskExecute.getJobId()));

        try {

            int taskStatus = JobTaskBatchStatusEnum.RUNNING.getStatus();
            int operationReason = JobOperationReasonEnum.NONE.getReason();
            if (Objects.isNull(job)) {
                taskStatus = JobTaskBatchStatusEnum.CANCEL.getStatus();
                operationReason = JobOperationReasonEnum.JOB_CLOSED.getReason();
            } else if (CollectionUtils.isEmpty(CacheRegisterTable.getServerNodeSet(job.getGroupName(),
                    job.getNamespaceId()))) {
                taskStatus = JobTaskBatchStatusEnum.CANCEL.getStatus();
                operationReason = JobOperationReasonEnum.NOT_CLIENT.getReason();
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {

                        if (Objects.nonNull(taskExecute.getWorkflowNodeId()) && Objects.nonNull(taskExecute.getWorkflowTaskBatchId())) {
                            // 若是工作流则开启下一个任务
                            try {
                                WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
                                taskExecuteDTO.setWorkflowTaskBatchId(taskExecute.getWorkflowTaskBatchId());
                                taskExecuteDTO.setTriggerType(JobExecuteStrategyEnum.AUTO.getType());
                                taskExecuteDTO.setParentId(taskExecute.getWorkflowNodeId());
                                taskExecuteDTO.setTaskBatchId(taskExecute.getTaskBatchId());
                                ActorRef actorRef = ActorGenerator.workflowTaskExecutorActor();
                                actorRef.tell(taskExecuteDTO, actorRef);
                            } catch (Exception e) {
                                log.error("任务调度执行失败", e);
                            }
                        }
                    }
                });

            }

            // 更新状态
            handlerTaskBatch(taskExecute, taskStatus, operationReason);

            // 不是运行中的，不需要生产任务
            if (taskStatus != JobTaskBatchStatusEnum.RUNNING.getStatus()) {
                return;
            }

            // 执行任务
            JobExecutor jobExecutor = JobExecutorFactory.getJobExecutor(job.getTaskType());
            JobExecutorContext context = JobTaskConverter.INSTANCE.toJobExecutorContext(job);
            context.setTaskBatchId(taskExecute.getTaskBatchId());
            context.setJobId(job.getId());
            context.setWorkflowTaskBatchId(taskExecute.getWorkflowTaskBatchId());
            context.setWorkflowNodeId(taskExecute.getWorkflowNodeId());
            jobExecutor.execute(context);
        } finally {
            log.info("准备执行任务完成.[{}]", JsonUtil.toJsonString(taskExecute));
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    // 清除时间轮的缓存
                    JobTimerWheel.clearCache(taskExecute.getTaskBatchId());
                    //方法内容
                    doHandlerResidentTask(job, taskExecute);
                }
            });
        }

    }

    private void handlerTaskBatch(TaskExecuteDTO taskExecute, int taskStatus, int operationReason) {

        JobTaskBatch jobTaskBatch = new JobTaskBatch();
        jobTaskBatch.setId(taskExecute.getTaskBatchId());
        jobTaskBatch.setExecutionAt(DateUtils.toNowMilli());
        jobTaskBatch.setTaskBatchStatus(taskStatus);
        jobTaskBatch.setOperationReason(operationReason);
        Assert.isTrue(1 == jobTaskBatchMapper.updateById(jobTaskBatch),
                () -> new EasyRetryServerException("更新任务失败"));

    }

    private void doHandlerResidentTask(Job job, TaskExecuteDTO taskExecuteDTO) {
        if (Objects.isNull(job)
            || JobExecuteStrategyEnum.MANUAL.getType().equals(taskExecuteDTO.getTriggerType())
            || JobExecuteStrategyEnum.WORKFLOW.getType().equals(taskExecuteDTO.getTriggerType())
            // 是否是常驻任务
            || Objects.equals(StatusEnum.NO.getStatus(), job.getResident())
        ) {
            return;
        }

        JobTimerTaskDTO jobTimerTaskDTO = new JobTimerTaskDTO();
        jobTimerTaskDTO.setJobId(taskExecuteDTO.getJobId());
        jobTimerTaskDTO.setTaskBatchId(taskExecuteDTO.getTaskBatchId());
        jobTimerTaskDTO.setTriggerType(JobExecuteStrategyEnum.AUTO.getType());
        ResidentJobTimerTask timerTask = new ResidentJobTimerTask(jobTimerTaskDTO, job);
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

        log.info("常驻任务监控. 任务时间差:[{}] 取余:[{}]", milliseconds, DateUtils.toNowMilli() % 1000);
        job.setNextTriggerAt(nextTriggerAt);

        JobTimerWheel.register(jobTimerTaskDTO.getTaskBatchId(), timerTask, milliseconds - DateUtils.toNowMilli() % 1000, TimeUnit.MILLISECONDS);
        ResidentTaskCache.refresh(job.getId(), nextTriggerAt);
    }
}
