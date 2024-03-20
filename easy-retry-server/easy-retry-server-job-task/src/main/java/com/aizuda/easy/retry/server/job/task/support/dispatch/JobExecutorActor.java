package com.aizuda.easy.retry.server.job.task.support.dispatch;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.WaitStrategy;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.common.dto.DistributeInstance;
import com.aizuda.easy.retry.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.easy.retry.server.common.enums.SyetemTaskTypeEnum;
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
import com.aizuda.easy.retry.server.job.task.support.generator.task.JobTaskGenerateContext;
import com.aizuda.easy.retry.server.job.task.support.generator.task.JobTaskGenerator;
import com.aizuda.easy.retry.server.job.task.support.generator.task.JobTaskGeneratorFactory;
import com.aizuda.easy.retry.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.easy.retry.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.easy.retry.server.job.task.support.timer.ResidentJobTimerTask;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.GroupConfigMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.GroupConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
import java.util.List;
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
@RequiredArgsConstructor
public class JobExecutorActor extends AbstractActor {
    private final JobMapper jobMapper;
    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final TransactionTemplate transactionTemplate;
    private final GroupConfigMapper groupConfigMapper;
    private final WorkflowBatchHandler workflowBatchHandler;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(TaskExecuteDTO.class, taskExecute -> {
            try {
                log.debug("准备执行任务. [{}] [{}]", LocalDateTime.now(), JsonUtil.toJsonString(taskExecute));
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(final TransactionStatus status) {
                        doExecute(taskExecute);
                    }
                });

            } catch (Exception e) {
                EasyRetryLog.LOCAL.error("job executor exception. [{}]", taskExecute, e);
                handlerTaskBatch(taskExecute, JobTaskBatchStatusEnum.FAIL.getStatus(), JobOperationReasonEnum.TASK_EXECUTION_ERROR.getReason());
                SpringContext.CONTEXT.publishEvent(new JobTaskFailAlarmEvent(taskExecute.getTaskBatchId()));
            } finally {
                getContext().stop(getSelf());
            }
        }).build();
    }

    private void doExecute(final TaskExecuteDTO taskExecute) {

        LambdaQueryWrapper<Job> queryWrapper = new LambdaQueryWrapper<>();
        // 自动地校验任务必须是开启状态，手动触发无需校验
        if (JobTaskExecutorSceneEnum.AUTO_JOB.getType().equals(taskExecute.getTaskExecutorScene())) {
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

                WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
                taskExecuteDTO.setWorkflowTaskBatchId(taskExecute.getWorkflowTaskBatchId());
                taskExecuteDTO.setTaskExecutorScene(taskExecute.getTaskExecutorScene());
                taskExecuteDTO.setParentId(taskExecute.getWorkflowNodeId());
                taskExecuteDTO.setTaskBatchId(taskExecute.getTaskBatchId());
                workflowBatchHandler.openNextNode(taskExecuteDTO);
            }

            // 更新状态
            handlerTaskBatch(taskExecute, taskStatus, operationReason);

            // 不是运行中的，不需要生产任务
            if (taskStatus != JobTaskBatchStatusEnum.RUNNING.getStatus()) {
                return;
            }

            // 生成任务
            JobTaskGenerator taskInstance = JobTaskGeneratorFactory.getTaskInstance(job.getTaskType());
            JobTaskGenerateContext instanceGenerateContext = JobTaskConverter.INSTANCE.toJobTaskInstanceGenerateContext(job);
            instanceGenerateContext.setTaskBatchId(taskExecute.getTaskBatchId());
            List<JobTask> taskList = taskInstance.generate(instanceGenerateContext);
            if (CollectionUtils.isEmpty(taskList)) {
                return;
            }

            // 执行任务
            JobExecutor jobExecutor = JobExecutorFactory.getJobExecutor(job.getTaskType());
            jobExecutor.execute(buildJobExecutorContext(taskExecute, job, taskList));
        } finally {
            log.debug("准备执行任务完成.[{}]", JsonUtil.toJsonString(taskExecute));
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    // 清除时间轮的缓存
                    JobTimerWheel.clearCache(SyetemTaskTypeEnum.JOB.getType(), taskExecute.getTaskBatchId());
                    //方法内容
                    doHandlerResidentTask(job, taskExecute);
                }
            });
        }

    }

    @NotNull
    private static JobExecutorContext buildJobExecutorContext(TaskExecuteDTO taskExecute, Job job, List<JobTask> taskList) {
        JobExecutorContext context = JobTaskConverter.INSTANCE.toJobExecutorContext(job);
        context.setTaskList(taskList);
        context.setTaskBatchId(taskExecute.getTaskBatchId());
        context.setJobId(job.getId());
        context.setWorkflowTaskBatchId(taskExecute.getWorkflowTaskBatchId());
        context.setWorkflowNodeId(taskExecute.getWorkflowNodeId());
        return context;
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

        log.debug("常驻任务监控. 任务时间差:[{}] 取余:[{}]", milliseconds, DateUtils.toNowMilli() % 1000);
        job.setNextTriggerAt(nextTriggerAt);

        JobTimerWheel.register(SyetemTaskTypeEnum.JOB.getType(), jobTimerTaskDTO.getTaskBatchId(), timerTask, milliseconds - DateUtils.toNowMilli() % 1000, TimeUnit.MILLISECONDS);
        ResidentTaskCache.refresh(job.getId(), nextTriggerAt);
    }
}
