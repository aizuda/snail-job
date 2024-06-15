package com.aizuda.snailjob.client.job.core.executor;

import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.job.core.dto.MapArgs;
import com.aizuda.snailjob.client.job.core.dto.MapReduceArgs;
import com.aizuda.snailjob.client.job.core.dto.ReduceArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.core.enums.MapReduceStageEnum;
import com.aizuda.snailjob.common.core.exception.SnailJobMapReduceException;
import com.aizuda.snailjob.common.core.model.JobContext;

import java.util.List;

/**
 * @author zhengweilin
 * @version 1.0.0
 * @date 2024/06/12
 */
public abstract class AbstractMapReduceExecutor extends AbstractMapExecutor {

    @Override
    public ExecuteResult doJobExecute(final JobArgs jobArgs) {
        JobContext jobContext = JobContextManager.getJobContext();
        if (jobContext.getMrStage().equals(MapReduceStageEnum.MAP.getStage())) {
           return super.doJobExecute(jobArgs);
        } else if(jobContext.getMrStage().equals(MapReduceStageEnum.REDUCE.getStage())) {
            ReduceArgs reduceArgs = (ReduceArgs) jobArgs;
            return doReduceExecute(reduceArgs);
        }

        throw new SnailJobMapReduceException("非法的MapReduceStage");
    }

    protected abstract ExecuteResult doReduceExecute(ReduceArgs reduceArgs);
}
