package com.aizuda.snailjob.server.job.task.support.result.job;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.snailjob.server.job.task.support.JobExecutorResultHandler;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.JobTaskStopHandler;
import com.aizuda.snailjob.server.job.task.support.alarm.event.JobTaskFailAlarmEvent;
import com.aizuda.snailjob.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.snailjob.server.job.task.support.stop.JobTaskStopFactory;
import com.aizuda.snailjob.server.job.task.support.stop.TaskStopJobContext;
import com.aizuda.snailjob.template.datasource.persistence.mapper.GroupConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: opensnail
 * @date : 2024-09-04
 * @since :1.2.0
 */
@RequiredArgsConstructor
public abstract class AbstractJobExecutorResultHandler implements JobExecutorResultHandler {

    private final JobTaskMapper jobTaskMapper;
    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final WorkflowBatchHandler workflowBatchHandler;
    private final GroupConfigMapper groupConfigMapper;

    @Override
    public void handleResult(final JobExecutorResultContext context) {

        List<JobTask> jobTasks = jobTaskMapper.selectList(
                new LambdaQueryWrapper<JobTask>()
                        .select(JobTask::getTaskStatus, JobTask::getMrStage)
                        .eq(JobTask::getTaskBatchId, context.getTaskBatchId()));

        if (CollUtil.isEmpty(jobTasks) ||
                jobTasks.stream().anyMatch(jobTask -> JobTaskStatusEnum.NOT_COMPLETE.contains(jobTask.getTaskStatus()))) {
            return;
        }

        // 放入任务项, 子类会用到
        context.setJobTaskList(jobTasks);

        Map<Integer, Long> statusCountMap = jobTasks.stream()
                .collect(Collectors.groupingBy(JobTask::getTaskStatus, Collectors.counting()));

        long failCount = statusCountMap.getOrDefault(JobTaskBatchStatusEnum.FAIL.getStatus(), 0L);
        long stopCount = statusCountMap.getOrDefault(JobTaskBatchStatusEnum.STOP.getStatus(), 0L);

        int taskBatchStatus;
        if (failCount > 0) {
            taskBatchStatus = JobTaskBatchStatusEnum.FAIL.getStatus();
            SnailSpringContext.getContext().publishEvent(new JobTaskFailAlarmEvent(context.getTaskBatchId()));
            doHandleFail(context);
        } else if (stopCount > 0) {
            taskBatchStatus = JobTaskBatchStatusEnum.STOP.getStatus();
            doHandleStop(context);
        } else {
            taskBatchStatus = JobTaskBatchStatusEnum.SUCCESS.getStatus();
            doHandleSuccess(context);
        }

        // 开启下一个工作流节点
        openNextWorkflowNode(context);

        boolean res = updateStatus(context, taskBatchStatus);
        context.setTaskBatchComplete(res);
        if (res) {
            // 停止客户端的任务
            stop(context);
        }
    }

    protected void openNextWorkflowNode(final JobExecutorResultContext context) {
        WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
        taskExecuteDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
        taskExecuteDTO.setTaskExecutorScene(JobTaskExecutorSceneEnum.AUTO_WORKFLOW.getType());
        taskExecuteDTO.setParentId(context.getWorkflowNodeId());
        taskExecuteDTO.setTaskBatchId(context.getTaskBatchId());
        workflowBatchHandler.openNextNode(taskExecuteDTO);
    }

    protected boolean updateStatus(final JobExecutorResultContext context, final Integer taskBatchStatus) {
        JobTaskBatch jobTaskBatch = new JobTaskBatch();
        jobTaskBatch.setId(context.getTaskBatchId());
        jobTaskBatch.setTaskBatchStatus(taskBatchStatus);
        jobTaskBatch.setUpdateDt(LocalDateTime.now());
        jobTaskBatch.setOperationReason(
                Optional.ofNullable(context.getJobOperationReason()).orElse(JobOperationReasonEnum.NONE.getReason())
        );

        if (JobTaskBatchStatusEnum.NOT_SUCCESS.contains(taskBatchStatus) && context.isRetry()) {
            jobTaskBatchMapper.update(jobTaskBatch,
                    new LambdaUpdateWrapper<JobTaskBatch>()
                            .eq(JobTaskBatch::getId, context.getTaskBatchId()));
            return false;
        }

        return 1 == jobTaskBatchMapper.update(jobTaskBatch,
                new LambdaUpdateWrapper<JobTaskBatch>()
                        .eq(JobTaskBatch::getId, context.getTaskBatchId())
                        .in(!context.isRetry(), JobTaskBatch::getTaskBatchStatus, JobTaskBatchStatusEnum.NOT_COMPLETE)
        );
    }

    protected void stop(JobExecutorResultContext context) {
        JobTaskStopHandler instanceInterrupt = JobTaskStopFactory.getJobTaskStop(getTaskInstanceType().getType());
        TaskStopJobContext stopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(context);
        stopJobContext.setNeedUpdateTaskStatus(Boolean.FALSE);
        stopJobContext.setForceStop(Boolean.TRUE);
        instanceInterrupt.stop(stopJobContext);
    }

    protected abstract void doHandleSuccess(final JobExecutorResultContext context);

    protected abstract void doHandleStop(final JobExecutorResultContext context);

    protected abstract void doHandleFail(final JobExecutorResultContext context);

}
