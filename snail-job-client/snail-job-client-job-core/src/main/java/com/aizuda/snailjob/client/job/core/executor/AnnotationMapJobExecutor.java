package com.aizuda.snailjob.client.job.core.executor;

import com.aizuda.snailjob.client.job.core.MapHandler;
import com.aizuda.snailjob.client.job.core.cache.JobExecutorInfoCache;
import com.aizuda.snailjob.client.job.core.dto.JobExecutorInfo;
import com.aizuda.snailjob.client.job.core.dto.MapArgs;
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
 * 基于注解的Map任务执行器
 *
 * @author opensnail
 * @date 2024-06-26 22:20:36
 * @since sj_1.1.0
 */
@Component
public class AnnotationMapJobExecutor extends AbstractMapExecutor {

    @Override
    public ExecuteResult doJobMapExecute(final MapArgs mapArgs, final MapHandler mapHandler) {
        return invokeMapExecute(mapArgs, mapHandler);
    }
}
