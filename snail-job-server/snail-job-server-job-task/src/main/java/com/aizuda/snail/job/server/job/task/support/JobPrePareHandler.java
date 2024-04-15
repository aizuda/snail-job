package com.aizuda.snail.job.server.job.task.support;

import com.aizuda.snail.job.server.job.task.dto.JobTaskPrepareDTO;

/**
 * @author opensnail
 * @date 2023-10-02 09:34:00
 * @since 2.4.0
 */
public interface JobPrePareHandler {

    boolean matches(Integer status);

    void handler(JobTaskPrepareDTO jobPrepareDTO);
}
