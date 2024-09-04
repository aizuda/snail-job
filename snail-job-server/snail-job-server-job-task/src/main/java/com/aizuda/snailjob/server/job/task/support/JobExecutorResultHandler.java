package com.aizuda.snailjob.server.job.task.support;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.snailjob.server.job.task.support.result.job.JobExecutorResultContext;

/**
 * @author: opensnail
 * @date : 2024-09-04
 * @since :1.2.0
 */
public interface JobExecutorResultHandler {

    JobTaskTypeEnum getTaskInstanceType();

    void handleResult(JobExecutorResultContext context);
}
