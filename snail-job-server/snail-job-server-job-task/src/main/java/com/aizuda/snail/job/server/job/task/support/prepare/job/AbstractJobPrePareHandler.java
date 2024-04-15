package com.aizuda.snail.job.server.job.task.support.prepare.job;

import com.aizuda.snail.job.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.snail.job.server.job.task.support.JobPrePareHandler;

/**
 * @author opensnail
 * @date 2023-10-02 09:57:55
 * @since 2.4.0
 */
public abstract class AbstractJobPrePareHandler implements JobPrePareHandler {

    @Override
    public void handler(JobTaskPrepareDTO jobPrepareDTO) {

        doHandler(jobPrepareDTO);
    }

    protected abstract void doHandler(JobTaskPrepareDTO jobPrepareDTO);
}
