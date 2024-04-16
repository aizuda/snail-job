package com.aizuda.snailjob.server.job.task.support;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.server.job.task.support.executor.job.JobExecutorContext;
import com.aizuda.snailjob.server.job.task.support.executor.job.JobExecutorContext;

/**
 * @author opensnail
 * @date 2023-09-24 11:40:21
 * @since 2.4.0
 */
public interface JobExecutor {

    JobTaskTypeEnum getTaskInstanceType();

    void execute(JobExecutorContext context);
}
