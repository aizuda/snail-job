package com.aizuda.snailjob.server.job.task.support.timer;

import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.JobNotifySceneEnum;
import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.TimerTask;
import com.aizuda.snailjob.server.job.task.dto.JobTaskFailAlarmEventDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.JobTaskStopHandler;
import com.aizuda.snailjob.server.job.task.support.alarm.event.JobTaskFailAlarmEvent;
import com.aizuda.snailjob.server.job.task.support.stop.JobTaskStopFactory;
import com.aizuda.snailjob.server.job.task.support.stop.TaskStopJobContext;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import io.netty.util.Timeout;
import lombok.AllArgsConstructor;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * 任务超时检查
 *
 * @author opensnail
 * @date 2024-05-20 21:16:09
 * @since sj_1.0.0
 */
@AllArgsConstructor
public class JobTimeoutCheckTask implements TimerTask<String> {
    private static final String IDEMPOTENT_KEY_PREFIX = "job_timeout_check_{0}";

    private final Long taskBatchId;
    private final Long jobId;

    @Override
    public void run(Timeout timeout) throws Exception {
        JobTimerWheel.clearCache(idempotentKey());
        JobTaskBatchMapper jobTaskBatchMapper = SnailSpringContext.getBean(JobTaskBatchMapper.class);
        JobTaskBatch jobTaskBatch = jobTaskBatchMapper.selectById(taskBatchId);
        if (Objects.isNull(jobTaskBatch)) {
            SnailJobLog.LOCAL.error("jobTaskBatch:[{}] does not exist", taskBatchId);
            return;
        }

        // 已经完成了，无需重复停止任务
        if (JobTaskBatchStatusEnum.COMPLETED.contains(jobTaskBatch.getTaskBatchStatus())) {
            return;
        }

        JobMapper jobMapper = SnailSpringContext.getBean(JobMapper.class);
        Job job = jobMapper.selectById(jobId);
        if (Objects.isNull(job)) {
            SnailJobLog.LOCAL.error("job:[{}] does not exist", jobId);
            return;
        }

        // 超时停止任务
        JobTaskStopHandler instanceInterrupt = JobTaskStopFactory.getJobTaskStop(job.getTaskType());
        TaskStopJobContext stopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(job);
        stopJobContext.setJobOperationReason(JobOperationReasonEnum.TASK_EXECUTION_TIMEOUT.getReason());
        stopJobContext.setNeedUpdateTaskStatus(Boolean.TRUE);
        stopJobContext.setForceStop(Boolean.TRUE);
        stopJobContext.setTaskBatchId(taskBatchId);
        stopJobContext.setWorkflowNodeId(jobTaskBatch.getWorkflowNodeId());
        stopJobContext.setWorkflowTaskBatchId(jobTaskBatch.getWorkflowTaskBatchId());
        instanceInterrupt.stop(stopJobContext);

        String reason = "Timeout interruption. Task batch ID:[" + taskBatchId + "]";
        SnailSpringContext.getContext().publishEvent(
                new JobTaskFailAlarmEvent(JobTaskFailAlarmEventDTO.builder()
                        .jobTaskBatchId(taskBatchId)
                        .reason(reason)
                        .notifyScene(JobNotifySceneEnum.JOB_TASK_ERROR.getNotifyScene())
                        .build()));
        SnailJobLog.LOCAL.info(reason);
    }

    @Override
    public String idempotentKey() {
        return MessageFormat.format(IDEMPOTENT_KEY_PREFIX, taskBatchId);
    }
}
