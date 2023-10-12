package com.aizuda.easy.retry.server.retry.task.support.dispatch.task;

import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;

/**
 * @author www.byteblogs.com
 * @date 2023-09-23 08:01:38
 * @since 2.4.0
 */
public interface TaskExecutor {

    TaskExecutorSceneEnum getTaskType();

    void actuator(RetryTask retryTask);
}
