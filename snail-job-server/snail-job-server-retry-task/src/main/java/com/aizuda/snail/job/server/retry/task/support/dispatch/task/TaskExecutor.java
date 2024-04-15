package com.aizuda.snail.job.server.retry.task.support.dispatch.task;

import com.aizuda.snail.job.template.datasource.persistence.po.RetryTask;

/**
 * @author opensnail
 * @date 2023-09-23 08:01:38
 * @since 2.4.0
 */
public interface TaskExecutor {

    TaskExecutorSceneEnum getTaskType();

    void actuator(RetryTask retryTask);
}
