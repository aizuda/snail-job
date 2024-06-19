package com.aizuda.snailjob.server.job.task.support.executor.job;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import org.springframework.stereotype.Component;

/**
 * @author: shuguang.zhang
 * @date : 2024-06-19
 */
@Component
public class MapJobExecutor extends MapReduceJobExecutor {

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.MAP;
    }

    @Override
    protected void doExecute(final JobExecutorContext context) {
        super.doExecute(context);
    }
}
