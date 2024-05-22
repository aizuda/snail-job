package com.aizuda.snailjob.server.job.task.support.timer;

import com.aizuda.snailjob.common.core.context.SpringContext;
import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
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

import java.util.Objects;

/**
 * 任务超时检查
 *
 * @author opensnail
 * @date 2024-05-20 21:16:09
 * @since sj_1.0.0
 */
@AllArgsConstructor
public class JobTimeoutCheckTask implements TimerTask<Long> {
    private final Long taskBatchId;
    private final Long jobId;

    @Override
    public void run(Timeout timeout) throws Exception {
        JobTaskBatchMapper jobTaskBatchMapper = SpringContext.getBean(JobTaskBatchMapper.class);
        JobTaskBatch jobTaskBatch = jobTaskBatchMapper.selectById(taskBatchId);
        if (Objects.isNull(jobTaskBatch)) {
            SnailJobLog.LOCAL.error("jobTaskBatch:[{}]不存在", taskBatchId);
            return;
        }

        // 已经完成了，无需重复停止任务
        if (JobTaskBatchStatusEnum.COMPLETED.contains(jobTaskBatch.getTaskBatchStatus())) {
            return;
        }

        JobMapper jobMapper = SpringContext.getBean(JobMapper.class);
        Job job = jobMapper.selectById(jobId);
        if (Objects.isNull(job)) {
            SnailJobLog.LOCAL.error("job:[{}]不存在", jobId);
            return;
        }

        // 超时停止任务
        JobTaskStopHandler instanceInterrupt = JobTaskStopFactory.getJobTaskStop(job.getTaskType());
        TaskStopJobContext stopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(job);
        stopJobContext.setJobOperationReason(JobOperationReasonEnum.TASK_EXECUTION_TIMEOUT.getReason());
        stopJobContext.setNeedUpdateTaskStatus(Boolean.TRUE);
        stopJobContext.setForceStop(Boolean.TRUE);
        stopJobContext.setTaskBatchId(taskBatchId);
        instanceInterrupt.stop(stopJobContext);

        SpringContext.getContext().publishEvent(new JobTaskFailAlarmEvent(taskBatchId));
        SnailJobLog.LOCAL.info("超时中断.taskBatchId:[{}]", taskBatchId);
    }

    @Override
    public Long getUniqueId() {
        return taskBatchId;
    }
}
