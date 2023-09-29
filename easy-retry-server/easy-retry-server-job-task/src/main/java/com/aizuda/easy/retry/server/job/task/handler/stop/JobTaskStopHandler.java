package com.aizuda.easy.retry.server.job.task.handler.stop;

import com.aizuda.easy.retry.server.job.task.enums.TaskTypeEnum;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 10:43:58
 * @since 2.4.0
 */
public interface JobTaskStopHandler {

    TaskTypeEnum getTaskType();

    void stop(TaskStopJobContext context);

}
