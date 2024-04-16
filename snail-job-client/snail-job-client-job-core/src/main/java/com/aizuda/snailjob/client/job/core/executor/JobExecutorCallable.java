package com.aizuda.snailjob.client.job.core.executor;

import com.aizuda.snailjob.client.model.ExecuteResult;

import java.util.concurrent.Callable;

/**
 * @author opensnail
 * @date 2023-10-08 22:48:44
 * @since 2.4.0
 */
public class JobExecutorCallable implements Callable<ExecuteResult> {

    public JobExecutorCallable(ExecuteResult executeResult) {

    }

    @Override
    public ExecuteResult call() throws Exception {

        return null;
    }
}
