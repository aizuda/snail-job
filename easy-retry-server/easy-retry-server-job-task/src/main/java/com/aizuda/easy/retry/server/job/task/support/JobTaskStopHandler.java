package com.aizuda.easy.retry.server.job.task.support;

import com.aizuda.easy.retry.common.core.enums.JobTaskTypeEnum;
import com.aizuda.easy.retry.server.job.task.support.stop.TaskStopJobContext;

/**
 * @author opensnail
 * @date 2023-10-02 10:43:58
 * @since 2.4.0
 */
public interface JobTaskStopHandler {

    JobTaskTypeEnum getTaskType();

    void stop(TaskStopJobContext context);

}
