package com.aizuda.snail.job.server.job.task.support.executor.job;

import com.aizuda.snail.job.server.job.task.support.JobExecutor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author opensnail
 * @date 2023-10-03 22:13:04
 * @since 2.4.0
 */
public abstract class AbstractJobExecutor implements JobExecutor, InitializingBean {

    @Override
    @Transactional
    public void execute(JobExecutorContext context) {
        doExecute(context);
    }

    protected abstract void doExecute(JobExecutorContext context);

    @Override
    public void afterPropertiesSet() throws Exception {
        JobExecutorFactory.registerJobExecutor(getTaskInstanceType(), this);
    }
}
