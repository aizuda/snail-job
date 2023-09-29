package com.aizuda.easy.retry.client.job.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author www.byteblogs.com
 * @date 2023-09-27 22:34:29
 * @since 2.4.0
 */
@Data
@AllArgsConstructor
public class JobExecutorInfo {

    private final String executorName;

    private final Method method;

    private Object executor;

    private boolean isAnnotation;

}
