package com.aizuda.snailjob.server.job.task.support.dispatch;

import akka.actor.AbstractActor;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.*;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.JobTaskFailAlarmEventDTO;
import com.aizuda.snailjob.server.job.task.dto.TaskExecuteDTO;
import com.aizuda.snailjob.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.snailjob.server.job.task.support.JobExecutor;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.alarm.event.JobTaskFailAlarmEvent;
import com.aizuda.snailjob.server.job.task.support.executor.job.JobExecutorContext;
import com.aizuda.snailjob.server.job.task.support.executor.job.JobExecutorFactory;
import com.aizuda.snailjob.server.job.task.support.generator.task.JobTaskGenerateContext;
import com.aizuda.snailjob.server.job.task.support.generator.task.JobTaskGenerator;
import com.aizuda.snailjob.server.job.task.support.generator.task.JobTaskGeneratorFactory;
import com.aizuda.snailjob.server.job.task.support.handler.JobTaskBatchHandler;
import com.aizuda.snailjob.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimeoutCheckTask;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerTask;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum.MAP;
import static com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum.MAP_REDUCE;

/**
 * @author: opensnail
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
    private final WorkflowBatchHandler workflowBatchHandler;
    private final JobTaskBatchHandler jobTaskBatchHandler;
    private final WorkflowTaskBatchMapper workflowTaskBatchMapper;

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
                SnailJobLog.LOCAL.error("job executor exception. [{}]", taskExecute, e);
                handleTaskBatch(taskExecute, JobTaskBatchStatusEnum.FAIL.getStatus(), JobOperationReasonEnum.TASK_EXECUTION_ERROR.getReason());
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
        int taskStatus = JobTaskBatchStatusEnum.RUNNING.getStatus();
        try {
            int operationReason = JobOperationReasonEnum.NONE.getReason();
            if (Objects.isNull(job)) {
                taskStatus = JobTaskBatchStatusEnum.CANCEL.getStatus();
                operationReason = JobOperationReasonEnum.JOB_CLOSED.getReason();
            } else if (CollUtil.isEmpty(CacheRegisterTable.getServerNodeSet(job.getGroupName(), job.getNamespaceId()))) {
                taskStatus = JobTaskBatchStatusEnum.CANCEL.getStatus();
                operationReason = JobOperationReasonEnum.NOT_CLIENT.getReason();

                WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
                taskExecuteDTO.setWorkflowTaskBatchId(taskExecute.getWorkflowTaskBatchId());
                taskExecuteDTO.setTaskExecutorScene(taskExecute.getTaskExecutorScene());
                taskExecuteDTO.setParentId(taskExecute.getWorkflowNodeId());
                taskExecuteDTO.setTaskBatchId(taskExecute.getTaskBatchId());
                workflowBatchHandler.openNextNode(taskExecuteDTO);
            }

            // 无客户端节点-告警通知
            if (JobTaskBatchStatusEnum.CANCEL.getStatus() == taskStatus && JobOperationReasonEnum.NOT_CLIENT.getReason() == operationReason) {
                SnailSpringContext.getContext().publishEvent(
                        new JobTaskFailAlarmEvent(JobTaskFailAlarmEventDTO.builder()
                                .jobTaskBatchId(taskExecute.getTaskBatchId())
                                .reason(JobNotifySceneEnum.JOB_NO_CLIENT_NODES_ERROR.getDesc())
                                .notifyScene(JobNotifySceneEnum.JOB_NO_CLIENT_NODES_ERROR.getNotifyScene())
                                .build()));
                return;
            }

            // 更新状态
            handleTaskBatch(taskExecute, taskStatus, operationReason);

            // 不是运行中的，不需要生产任务
            if (taskStatus != JobTaskBatchStatusEnum.RUNNING.getStatus()) {
                return;
            }

            // 生成任务
            JobTaskGenerator taskInstance = JobTaskGeneratorFactory.getTaskInstance(job.getTaskType());
            JobTaskGenerateContext instanceGenerateContext = JobTaskConverter.INSTANCE.toJobTaskInstanceGenerateContext(job);
            instanceGenerateContext.setTaskBatchId(taskExecute.getTaskBatchId());
            if (Objects.nonNull(taskExecute.getTmpArgsStr())){
                instanceGenerateContext.setArgsStr(taskExecute.getTmpArgsStr());
            }
            if (Lists.newArrayList(MAP_REDUCE.getType(), MAP.getType()).contains(job.getTaskType())) {
                instanceGenerateContext.setTaskName(SystemConstants.ROOT_MAP);
                instanceGenerateContext.setMapSubTask(Lists.newArrayList(StrUtil.EMPTY));
                instanceGenerateContext.setMrStage(MapReduceStageEnum.MAP.getStage());
            }
            List<JobTask> taskList = taskInstance.generate(instanceGenerateContext);
            if (CollUtil.isEmpty(taskList)) {
                SnailJobLog.LOCAL.warn("Generate job task is empty, taskBatchId:[{}]", taskExecute.getTaskBatchId());
                return;
            }

            // 事务提交以后再执行任务
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 获取工作流的上下文
                    WorkflowTaskBatch workflowTaskBatch = null;
                    Long workflowTaskBatchId = taskExecute.getWorkflowTaskBatchId();
                    if (Objects.nonNull(workflowTaskBatchId)) {
                        workflowTaskBatch = workflowTaskBatchMapper.selectOne(
                                new LambdaQueryWrapper<WorkflowTaskBatch>()
                                        .select(WorkflowTaskBatch::getWfContext)
                                        .eq(WorkflowTaskBatch::getId, taskExecute.getWorkflowTaskBatchId())
                        );
                    }

                    // 执行任务
                    JobExecutor jobExecutor = JobExecutorFactory.getJobExecutor(job.getTaskType());
                    jobExecutor.execute(buildJobExecutorContext(taskExecute, job, taskList, workflowTaskBatch));
                }
            });

        } finally {
            log.debug("准备执行任务完成.[{}]", JsonUtil.toJsonString(taskExecute));
            final int finalTaskStatus = taskStatus;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    // 清除时间轮的缓存
                    JobTimerWheel.clearCache(MessageFormat.format(JobTimerTask.IDEMPOTENT_KEY_PREFIX, taskExecute.getTaskBatchId()));

                    if (JobTaskBatchStatusEnum.RUNNING.getStatus() == finalTaskStatus) {

                        // 运行中的任务，需要进行超时检查
                        JobTimerWheel.registerWithJob(() -> new JobTimeoutCheckTask(taskExecute.getTaskBatchId(), job.getId()),
                                // 加500ms是为了让尽量保证客户端自己先超时中断，防止客户端上报成功但是服务端已触发超时中断
                                Duration.ofMillis(DateUtils.toEpochMilli(job.getExecutorTimeout()) + 500));
                    }

                    // 开启下一个常驻任务
                    jobTaskBatchHandler.openResidentTask(job, taskExecute);
                }
            });
        }

    }

    @NotNull
    private static JobExecutorContext buildJobExecutorContext(TaskExecuteDTO taskExecute, Job job, List<JobTask> taskList,
                                                              final WorkflowTaskBatch workflowTaskBatch) {
        JobExecutorContext context = JobTaskConverter.INSTANCE.toJobExecutorContext(job);
        context.setTaskList(taskList);
        context.setTaskBatchId(taskExecute.getTaskBatchId());
        context.setJobId(job.getId());
        context.setWorkflowTaskBatchId(taskExecute.getWorkflowTaskBatchId());
        context.setWorkflowNodeId(taskExecute.getWorkflowNodeId());
        if (Objects.nonNull(workflowTaskBatch)) {
            context.setWfContext(workflowTaskBatch.getWfContext());
        }
        return context;
    }

    private void handleTaskBatch(TaskExecuteDTO taskExecute, int taskStatus, int operationReason) {

        JobTaskBatch jobTaskBatch = new JobTaskBatch();
        jobTaskBatch.setId(taskExecute.getTaskBatchId());
        jobTaskBatch.setExecutionAt(DateUtils.toNowMilli());
        jobTaskBatch.setTaskBatchStatus(taskStatus);
        jobTaskBatch.setOperationReason(operationReason);
        Assert.isTrue(1 == jobTaskBatchMapper.updateById(jobTaskBatch),
                () -> new SnailJobServerException("更新任务失败"));

        if (JobTaskBatchStatusEnum.NOT_SUCCESS.contains(taskStatus)) {
            SnailSpringContext.getContext().publishEvent(
                    new JobTaskFailAlarmEvent(JobTaskFailAlarmEventDTO.builder()
                            .jobTaskBatchId(taskExecute.getTaskBatchId())
                            .reason(JobOperationReasonEnum.TASK_EXECUTION_ERROR.getDesc())
                            .notifyScene(JobNotifySceneEnum.JOB_TASK_ERROR.getNotifyScene())
                            .build()));
        }
    }

}
