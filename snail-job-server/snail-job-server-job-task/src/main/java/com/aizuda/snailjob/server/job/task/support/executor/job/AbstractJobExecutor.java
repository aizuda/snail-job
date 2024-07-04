package com.aizuda.snailjob.server.job.task.support.executor.job;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.job.task.support.JobExecutor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author opensnail
 * @date 2023-10-03 22:13:04
 * @since 2.4.0
 */
public abstract class AbstractJobExecutor implements JobExecutor, InitializingBean {

    @Override
    public void execute(JobExecutorContext context) {
        if (CollUtil.isEmpty(context.getTaskList())) {
            SnailJobLog.LOCAL.warn("待执行的任务列表为空. taskBatchId:[{}]", context.getTaskBatchId());
            return;
        }
        doExecute(context);
    }

    protected abstract void doExecute(JobExecutorContext context);

    @Override
    public void afterPropertiesSet() throws Exception {
        JobExecutorFactory.registerJobExecutor(getTaskInstanceType(), this);
    }
}
