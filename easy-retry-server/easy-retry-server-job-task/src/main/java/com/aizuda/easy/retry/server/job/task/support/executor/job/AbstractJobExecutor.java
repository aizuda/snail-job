package com.aizuda.easy.retry.server.job.task.support.executor.job;

import com.aizuda.easy.retry.server.job.task.support.JobExecutor;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.generator.task.JobTaskGenerateContext;
import com.aizuda.easy.retry.server.job.task.support.generator.task.JobTaskGenerator;
import com.aizuda.easy.retry.server.job.task.support.generator.task.JobTaskGeneratorFactory;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author www.byteblogs.com
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
