package com.aizuda.easy.retry.server.job.task.support;

import com.aizuda.easy.retry.common.core.enums.JobTaskTypeEnum;
import com.aizuda.easy.retry.server.job.task.support.executor.job.JobExecutorContext;

/**
 * @author www.byteblogs.com
 * @date 2023-09-24 11:40:21
 * @since 2.4.0
 */
public interface JobExecutor {

    JobTaskTypeEnum getTaskInstanceType();

    void execute(JobExecutorContext context);
}
