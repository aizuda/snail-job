package com.aizuda.easy.retry.server.job.task.handler.executor;

import com.aizuda.easy.retry.server.job.task.enums.TaskTypeEnum;

/**
 * @author www.byteblogs.com
 * @date 2023-09-24 11:40:21
 * @since 2.4.0
 */
public interface JobExecutor {

    TaskTypeEnum getTaskInstanceType();

    void execute(JobExecutorContext context);
}
