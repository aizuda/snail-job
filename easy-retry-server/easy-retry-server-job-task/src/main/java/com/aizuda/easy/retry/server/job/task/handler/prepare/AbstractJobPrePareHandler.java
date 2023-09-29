package com.aizuda.easy.retry.server.job.task.handler.prepare;

import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;

/**
 * @author www.byteblogs.com
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
