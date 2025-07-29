package com.aizuda.snailjob.client.job.core.executor;

import com.aizuda.snailjob.client.job.core.MapHandler;
import com.aizuda.snailjob.client.job.core.dto.MapArgs;
import com.aizuda.snailjob.model.dto.ExecuteResult;
import org.springframework.stereotype.Component;

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
