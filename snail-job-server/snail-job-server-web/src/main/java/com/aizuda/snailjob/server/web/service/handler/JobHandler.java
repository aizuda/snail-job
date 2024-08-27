package com.aizuda.snailjob.server.web.service.handler;

import akka.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.TaskExecuteDTO;
import com.aizuda.snailjob.server.job.task.enums.JobRetrySceneEnum;
import com.aizuda.snailjob.server.job.task.support.ClientCallbackHandler;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.JobTaskStopHandler;
import com.aizuda.snailjob.server.job.task.support.callback.ClientCallbackContext;
import com.aizuda.snailjob.server.job.task.support.callback.ClientCallbackFactory;
import com.aizuda.snailjob.server.job.task.support.stop.JobTaskStopFactory;
import com.aizuda.snailjob.server.job.task.support.stop.TaskStopJobContext;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimeoutCheckTask;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.snailjob.template.datasource.persistence.mapper.*;
import com.aizuda.snailjob.template.datasource.persistence.po.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author: xiaowoniu
 * @date : 2024-02-26
 * @since : 3.1.0
 */
@Component
@RequiredArgsConstructor
public class JobHandler {

    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final JobMapper jobMapper;
    private final JobTaskMapper jobTaskMapper;
    private final WorkflowTaskBatchMapper workflowTaskBatchMapper;
    private final JobLogMessageMapper jobLogMessageMapper;

    public Boolean retry(Long taskBatchId) {
        return retry(taskBatchId, null, null);
    }

    public Boolean retry(Long taskBatchId, Long workflowNodeId, Long workflowTaskBatchId) {
        JobTaskBatch jobTaskBatch = jobTaskBatchMapper.selectOne(new LambdaQueryWrapper<JobTaskBatch>()
                .eq(JobTaskBatch::getId, taskBatchId)
                .in(JobTaskBatch::getTaskBatchStatus, JobTaskBatchStatusEnum.NOT_SUCCESS)
        );
        Assert.notNull(jobTaskBatch, () -> new SnailJobServerException("job batch can not be null."));

        // 重置状态为运行中
        jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.RUNNING.getStatus());
        // 重置状态原因
        jobTaskBatch.setOperationReason(JobOperationReasonEnum.NONE.getReason());
        Assert.isTrue(jobTaskBatchMapper.updateById(jobTaskBatch) > 0,
                () -> new SnailJobServerException("update job batch to running failed."));

        Job job = jobMapper.selectById(jobTaskBatch.getJobId());
        Assert.notNull(job, () -> new SnailJobServerException("job can not be null."));

        List<JobTask> jobTasks = jobTaskMapper.selectList(
                new LambdaQueryWrapper<JobTask>()
                        .select(JobTask::getId, JobTask::getTaskStatus)
                        .eq(JobTask::getTaskBatchId, taskBatchId));

        //  若任务项为空则生成
        if (CollUtil.isEmpty(jobTasks)) {
            TaskExecuteDTO taskExecuteDTO = new TaskExecuteDTO();
            taskExecuteDTO.setTaskBatchId(taskBatchId);
            taskExecuteDTO.setJobId(jobTaskBatch.getJobId());
            taskExecuteDTO.setTaskExecutorScene(JobTaskExecutorSceneEnum.MANUAL_JOB.getType());
            taskExecuteDTO.setWorkflowTaskBatchId(workflowTaskBatchId);
            taskExecuteDTO.setWorkflowNodeId(workflowNodeId);
            ActorRef actorRef = ActorGenerator.jobTaskExecutorActor();
            actorRef.tell(taskExecuteDTO, actorRef);

            return Boolean.TRUE;
        }

        // 获取工作流上下文
        String wfContext = getWfContext(workflowTaskBatchId);

        for (JobTask jobTask : jobTasks) {
            // 增加Map及MapReduce重试任务的状态判断，防止重复执行
            if (jobTask.getTaskStatus() == JobTaskStatusEnum.RUNNING.getStatus()
                    || jobTask.getTaskStatus() == JobTaskStatusEnum.SUCCESS.getStatus()) {
                continue;
            }

            jobTask.setTaskStatus(JobTaskStatusEnum.RUNNING.getStatus());
            Assert.isTrue(jobTaskMapper.updateById(jobTask) > 0,
                    () -> new SnailJobServerException("update job task to running failed."));
            // 模拟失败重试
            ClientCallbackHandler clientCallback = ClientCallbackFactory.getClientCallback(job.getTaskType());
            ClientCallbackContext context = JobTaskConverter.INSTANCE.toClientCallbackContext(job);
            context.setTaskBatchId(jobTaskBatch.getId());
            context.setWorkflowNodeId(workflowNodeId);
            context.setWorkflowTaskBatchId(workflowTaskBatchId);
            context.setTaskId(jobTask.getId());
            context.setTaskStatus(JobTaskStatusEnum.FAIL.getStatus());
            context.setRetryScene(JobRetrySceneEnum.MANUAL.getRetryScene());
            context.setWfContext(wfContext);
            context.setExecuteResult(ExecuteResult.failure(null, "手动重试"));
            clientCallback.callback(context);
        }

        // 运行中的任务，需要进行超时检查
        JobTimerWheel.registerWithJob(() -> new JobTimeoutCheckTask(taskBatchId, job.getId()),
                // 加500ms是为了让尽量保证客户端自己先超时中断，防止客户端上报成功但是服务端已触发超时中断
                Duration.ofMillis(DateUtils.toEpochMilli(job.getExecutorTimeout()) + 500));

        return Boolean.TRUE;
    }

    public Boolean stop(Long taskBatchId) {

        JobTaskBatch jobTaskBatch = jobTaskBatchMapper.selectById(taskBatchId);
        Assert.notNull(jobTaskBatch, () -> new SnailJobServerException("job batch can not be null."));

        Job job = jobMapper.selectById(jobTaskBatch.getJobId());
        Assert.notNull(job, () -> new SnailJobServerException("job can not be null."));

        JobTaskStopHandler jobTaskStop = JobTaskStopFactory.getJobTaskStop(job.getTaskType());

        TaskStopJobContext taskStopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(job);
        taskStopJobContext.setJobOperationReason(JobOperationReasonEnum.MANNER_STOP.getReason());
        taskStopJobContext.setTaskBatchId(jobTaskBatch.getId());
        taskStopJobContext.setForceStop(Boolean.TRUE);
        taskStopJobContext.setNeedUpdateTaskStatus(Boolean.TRUE);

        jobTaskStop.stop(taskStopJobContext);

        return Boolean.TRUE;
    }

    /**
     * 获取工作流批次
     *
     * @param workflowTaskBatchId 工作流批次
     * @return
     */
    private String getWfContext(Long workflowTaskBatchId) {
        if (Objects.isNull(workflowTaskBatchId)) {
            return null;
        }

        WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchMapper.selectOne(
                new LambdaQueryWrapper<WorkflowTaskBatch>()
                        .select(WorkflowTaskBatch::getWfContext)
                        .eq(WorkflowTaskBatch::getId, workflowTaskBatchId)
        );

        if (Objects.isNull(workflowTaskBatch)) {
            return null;
        }

        return workflowTaskBatch.getWfContext();
    }

    /**
     * 批次删除定时任务批次
     *
     * @param ids         任务批次id
     * @param namespaceId 命名空间
     */
    @Transactional
    public void deleteJobTaskBatchByIds(Set<Long> ids, String namespaceId) {
        // 1. 删除任务批次 job_task_batch
        Assert.isTrue(ids.size() == jobTaskBatchMapper.deleteByIds(ids),
                () -> new SnailJobServerException("删除任务批次失败"));

        // 2. 删除任务实例 job_task
        jobTaskMapper.delete(new LambdaQueryWrapper<JobTask>()
                .eq(JobTask::getNamespaceId, namespaceId)
                .in(JobTask::getTaskBatchId, ids));

        // 3. 删除调度日志 job_log_message
        jobLogMessageMapper.delete(new LambdaQueryWrapper<JobLogMessage>()
                .eq(JobLogMessage::getNamespaceId, namespaceId)
                .in(JobLogMessage::getTaskBatchId, ids)
        );
    }
}
