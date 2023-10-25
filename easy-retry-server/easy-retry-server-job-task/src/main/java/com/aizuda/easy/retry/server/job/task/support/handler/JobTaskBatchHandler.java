package com.aizuda.easy.retry.server.job.task.support.handler;

import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.JobTaskStopHandler;
import com.aizuda.easy.retry.server.job.task.support.stop.JobTaskStopFactory;
import com.aizuda.easy.retry.server.job.task.support.stop.TaskStopJobContext;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-10 16:50
 */
@Component
public class JobTaskBatchHandler {

    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;

    public boolean complete(Long taskBatchId, JobOperationReasonEnum jobOperationReasonEnum) {

        List<JobTask> jobTasks = jobTaskMapper.selectList(
            new LambdaQueryWrapper<JobTask>().select(JobTask::getTaskStatus)
                .eq(JobTask::getTaskBatchId, taskBatchId));


        if (jobTasks.stream().anyMatch(jobTask -> JobTaskStatusEnum.NOT_COMPLETE.contains(jobTask.getTaskStatus()))) {
            return false;
        }
        long failCount = jobTasks.stream().filter(jobTask -> jobTask.getTaskStatus() == JobTaskBatchStatusEnum.FAIL.getStatus()).count();
        long stopCount = jobTasks.stream().filter(jobTask -> jobTask.getTaskStatus() == JobTaskBatchStatusEnum.STOP.getStatus()).count();

        JobTaskBatch jobTaskBatch = new JobTaskBatch();
        jobTaskBatch.setId(taskBatchId);
        if (failCount > 0) {
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.FAIL.getStatus());
        } else if (stopCount > 0) {
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.STOP.getStatus());
        } else {
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.SUCCESS.getStatus());
        }

        if (Objects.nonNull(jobOperationReasonEnum)) {
            jobTaskBatch.setOperationReason(jobOperationReasonEnum.getReason());
        }

        return 1 == jobTaskBatchMapper.update(jobTaskBatch,
                new LambdaUpdateWrapper<JobTaskBatch>()
                        .eq(JobTaskBatch::getId, taskBatchId)
                        .in(JobTaskBatch::getTaskBatchStatus, JobTaskStatusEnum.NOT_COMPLETE)
        );

    }
}
