package com.aizuda.snailjob.client.job.core.executor;

import com.aizuda.snailjob.client.job.core.MapHandler;
import com.aizuda.snailjob.client.job.core.cache.JobExecutorInfoCache;
import com.aizuda.snailjob.client.job.core.dto.JobExecutorInfo;
import com.aizuda.snailjob.client.job.core.dto.MapArgs;
import com.aizuda.snailjob.client.job.core.dto.MergeReduceArgs;
import com.aizuda.snailjob.client.job.core.dto.ReduceArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.core.exception.SnailJobMapReduceException;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于注解的MapReduce执行器
 *
 * @author opensnail
 * @date 2024-06-26 22:20:36
 * @since sj_1.1.0
 */
@Component
public class AnnotationMapReduceJobExecutor extends AbstractMapReduceExecutor {

    @Override
    protected ExecuteResult doReduceExecute(final ReduceArgs reduceArgs) {
        JobExecutorInfo jobExecutorInfo = JobExecutorInfoCache.get(reduceArgs.getExecutorInfo());
        if (Objects.isNull(jobExecutorInfo)) {
            throw new SnailJobMapReduceException("[{}] not found", reduceArgs.getExecutorInfo());
        }

        if (Objects.isNull(jobExecutorInfo.getReduceExecutor())) {
            throw new SnailJobMapReduceException(
                "[{}] MapTask execution method not found. Please configure the @ReduceExecutor annotation",
                reduceArgs.getExecutorInfo());
        }

        Class<?>[] paramTypes = jobExecutorInfo.getReduceExecutor().getParameterTypes();
        if (paramTypes.length > 0) {
            return (ExecuteResult) ReflectionUtils.invokeMethod(jobExecutorInfo.getReduceExecutor(),
                jobExecutorInfo.getExecutor(), reduceArgs);
        }

        throw new SnailJobMapReduceException("[{}] ReduceTask execution method not found", reduceArgs.getExecutorInfo());
    }

    @Override
    protected ExecuteResult doMergeReduceExecute(final MergeReduceArgs mergeReduceArgs) {
        JobExecutorInfo jobExecutorInfo = JobExecutorInfoCache.get(mergeReduceArgs.getExecutorInfo());

        if (Objects.isNull(jobExecutorInfo)) {
            throw new SnailJobMapReduceException("[{}] not found", mergeReduceArgs.getExecutorInfo());
        }

        if (Objects.isNull(jobExecutorInfo.getReduceExecutor())) {
            throw new SnailJobMapReduceException(
                "[{}] MapTask execution method not found. Please configure the @MergeReduceExecutor annotation",
                mergeReduceArgs.getExecutorInfo());
        }

        Class<?>[] paramTypes = jobExecutorInfo.getMergeReduceExecutor().getParameterTypes();
        if (paramTypes.length > 0) {
            return (ExecuteResult) ReflectionUtils.invokeMethod(jobExecutorInfo.getReduceExecutor(),
                jobExecutorInfo.getExecutor(), mergeReduceArgs);
        }

        throw new SnailJobMapReduceException("[{}] MergeReduceTask execution method not found [{}]",
            mergeReduceArgs.getExecutorInfo());

    }

    @Override
    public ExecuteResult doJobMapExecute(final MapArgs mapArgs, final MapHandler mapHandler) {
        return invokeMapExecute(mapArgs, mapHandler);
    }
}
