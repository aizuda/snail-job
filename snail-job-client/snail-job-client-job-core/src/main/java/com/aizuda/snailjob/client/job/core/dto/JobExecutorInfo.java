package com.aizuda.snailjob.client.job.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author opensnail
 * @date 2023-09-27 22:34:29
 * @since 2.4.0
 */
@Data
@AllArgsConstructor
public class JobExecutorInfo {

    private final String executorName;

    private final Method method;

    private final Map<String, Method> mapExecutorMap;

    Method reduceExecutor;

    Method mergeReduceExecutor;

    private Object executor;

}
