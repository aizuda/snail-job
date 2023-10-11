package com.aizuda.easy.retry.server.job.task.handler.helper;

import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-10 16:50
 */
@Component
public class JobTaskBatchHelper {

    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;

    public boolean complete(Long taskBatchId) {

        List<JobTask> jobTasks = jobTaskMapper.selectList(
            new LambdaQueryWrapper<JobTask>().select(JobTask::getExecuteStatus)
                .eq(JobTask::getTaskBatchId, taskBatchId));

        if (jobTasks.stream().anyMatch(jobTask -> JobTaskStatusEnum.NOT_COMPLETE.contains(jobTask.getExecuteStatus()))) {
           return false;
        }

        long failCount = jobTasks.stream().filter(jobTask -> jobTask.getExecuteStatus() == JobTaskBatchStatusEnum.FAIL.getStatus()).count();
        long stopCount = jobTasks.stream().filter(jobTask -> jobTask.getExecuteStatus() == JobTaskBatchStatusEnum.STOP.getStatus()).count();

        JobTaskBatch jobTaskBatch = new JobTaskBatch();
        jobTaskBatch.setId(taskBatchId);
        if (failCount > 0) {
            jobTaskBatch.setTaskStatus(JobTaskBatchStatusEnum.FAIL.getStatus());
        } else if (stopCount > 0) {
            jobTaskBatch.setTaskStatus(JobTaskBatchStatusEnum.STOP.getStatus());
        } else {
            jobTaskBatch.setTaskStatus(JobTaskBatchStatusEnum.SUCCESS.getStatus());
        }

        jobTaskBatchMapper.updateById(jobTaskBatch);

        return true;

    }
}
