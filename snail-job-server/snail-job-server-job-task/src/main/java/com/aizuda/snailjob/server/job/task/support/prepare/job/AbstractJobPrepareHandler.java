package com.aizuda.snailjob.server.job.task.support.prepare.job;

import com.aizuda.snailjob.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.snailjob.server.job.task.support.JobPrepareHandler;

/**
 * @author opensnail
 * @date 2023-10-02 09:57:55
 * @since 2.4.0
 */
public abstract class AbstractJobPrepareHandler implements JobPrepareHandler {

    @Override
    public void handle(JobTaskPrepareDTO jobPrepareDTO) {

        doHandle(jobPrepareDTO);
    }

    protected abstract void doHandle(JobTaskPrepareDTO jobPrepareDTO);
}
