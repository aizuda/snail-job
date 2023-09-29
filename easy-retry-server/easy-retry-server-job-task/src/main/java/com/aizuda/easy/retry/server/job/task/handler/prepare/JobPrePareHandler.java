package com.aizuda.easy.retry.server.job.task.handler.prepare;

import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 09:34:00
 * @since 2.4.0
 */
public interface JobPrePareHandler {

    boolean matches(Integer status);

    void handler(JobTaskPrepareDTO jobPrepareDTO);
}
