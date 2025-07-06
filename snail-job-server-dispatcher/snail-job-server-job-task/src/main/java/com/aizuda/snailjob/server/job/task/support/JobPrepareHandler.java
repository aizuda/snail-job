package com.aizuda.snailjob.server.job.task.support;

import com.aizuda.snailjob.server.job.task.dto.JobTaskPrepareDTO;

/**
 * @author opensnail
 * @date 2023-10-02 09:34:00
 * @since 2.4.0
 */
public interface JobPrepareHandler {

    boolean matches(Integer status);

    void handle(JobTaskPrepareDTO jobPrepareDTO);
}
