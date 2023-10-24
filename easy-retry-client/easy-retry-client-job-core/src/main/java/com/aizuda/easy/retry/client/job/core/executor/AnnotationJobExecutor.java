package com.aizuda.easy.retry.client.job.core.executor;

import com.aizuda.easy.retry.client.job.core.cache.JobExecutorInfoCache;
import com.aizuda.easy.retry.client.job.core.dto.JobArgs;
import com.aizuda.easy.retry.client.job.core.dto.JobContext;
import com.aizuda.easy.retry.client.job.core.dto.JobExecutorInfo;
import com.aizuda.easy.retry.client.model.ExecuteResult;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * 基于注解的执行器
 *
 * @author www.byteblogs.com
 * @date 2023-09-27 22:20:36
 * @since 2.4.0
 */
@Component
public class AnnotationJobExecutor extends AbstractJobExecutor {

    @Override
    protected ExecuteResult doJobExecute(final JobArgs jobArgs) {
        JobExecutorInfo jobExecutorInfo = JobExecutorInfoCache.get(jobArgs.getExecutorInfo());
        Class<?>[] paramTypes = jobExecutorInfo.getMethod().getParameterTypes();

        if (paramTypes.length > 0) {
            return (ExecuteResult) ReflectionUtils.invokeMethod(jobExecutorInfo.getMethod(), jobExecutorInfo.getExecutor(), jobArgs);
        } else {
            return (ExecuteResult) ReflectionUtils.invokeMethod(jobExecutorInfo.getMethod(), jobExecutorInfo.getExecutor());
        }
    }
}
