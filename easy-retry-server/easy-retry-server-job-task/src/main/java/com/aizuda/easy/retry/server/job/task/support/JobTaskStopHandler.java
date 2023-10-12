package com.aizuda.easy.retry.server.job.task.support;

import com.aizuda.easy.retry.server.job.task.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.job.task.support.stop.TaskStopJobContext;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 10:43:58
 * @since 2.4.0
 */
public interface JobTaskStopHandler {

    TaskTypeEnum getTaskType();

    void stop(TaskStopJobContext context);

}
