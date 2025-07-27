package com.aizuda.snailjob.client.job.core.executor;

import com.aizuda.snailjob.client.job.core.IJobExecutor;
import com.aizuda.snailjob.client.job.core.MapHandler;
import com.aizuda.snailjob.client.job.core.cache.JobExecutorInfoCache;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.job.core.dto.JobExecutorInfo;
import com.aizuda.snailjob.client.job.core.dto.MapArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.core.exception.SnailJobMapReduceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author zhengweilin
 * @version 1.0.0
 * @date 2024/06/12
 */
@Slf4j
public abstract class AbstractMapExecutor extends AbstractJobExecutor implements IJobExecutor {

    @Override
    protected ExecuteResult doJobExecute(final JobArgs jobArgs) {
        if (jobArgs instanceof MapArgs) {
            return this.doJobMapExecute((MapArgs) jobArgs, getMapHandler());
        }

        throw new SnailJobMapReduceException("For tasks that are not of type map or map reduce, please do not use the AbstractMapExecutor class.");
    }

    public abstract ExecuteResult doJobMapExecute(MapArgs mapArgs, final MapHandler mapHandler);

    private MapHandler getMapHandler() {
       return (MapHandler) Proxy.newProxyInstance(MapHandler.class.getClassLoader(),
            new Class[]{MapHandler.class}, new MapInvokeHandler());
    }

    protected ExecuteResult invokeMapExecute (MapArgs mapArgs, final MapHandler mapHandler) {
        JobExecutorInfo jobExecutorInfo = JobExecutorInfoCache.get(mapArgs.getExecutorInfo());

        if (Objects.isNull(jobExecutorInfo)) {
            throw new SnailJobMapReduceException("[{}] not found", mapArgs.getExecutorInfo());
        }

        Map<String, Method> mapExecutorMap = Optional.ofNullable(jobExecutorInfo.getMapExecutorMap())
            .orElse(new HashMap<>());
        Method method = mapExecutorMap.get(mapArgs.getTaskName());

        if (Objects.isNull(method)) {
            throw new SnailJobMapReduceException(
                "[{}#{}] MapTask execution method not found. Please configure the @MapExecutor annotation",
                mapArgs.getExecutorInfo(), mapArgs.getTaskName());

        }

        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length == 1) {
            return (ExecuteResult) ReflectionUtils.invokeMethod(method, jobExecutorInfo.getExecutor(), mapArgs);
        } else if (paramTypes.length == 2) {
            return (ExecuteResult) ReflectionUtils.invokeMethod(method, jobExecutorInfo.getExecutor(), mapArgs,
                mapHandler);
        }

        throw new SnailJobMapReduceException("Executor for [{}] not found", mapArgs.getTaskName());
    }
}
