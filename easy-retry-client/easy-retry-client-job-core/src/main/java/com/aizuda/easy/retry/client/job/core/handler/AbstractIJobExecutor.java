package com.aizuda.easy.retry.client.job.core.handler;

import com.aizuda.easy.retry.client.job.core.IJobExecutor;
import com.aizuda.easy.retry.client.job.core.dto.ExecuteResult;
import com.aizuda.easy.retry.client.job.core.dto.JobContext;

/**
 * 广播模式
 *
 * @author: www.byteblogs.com
 * @date : 2023-09-27 09:48
 * @since 2.4.0
 */
public abstract class AbstractIJobExecutor implements IJobExecutor {

    @Override
    public ExecuteResult call() throws Exception {

        JobContext jobContext = new JobContext();
        ExecuteResult executeResult = jobExecute(jobContext);

        return executeResult;
    }

    protected abstract ExecuteResult jobExecute(JobContext jobContext);
}
