package com.aizuda.easy.retry.client.common.util;


import com.aizuda.easy.retry.common.core.model.JobContext;

/**
 * @author wodeyangzipingpingwuqi·
 * @date 2023-12-29
 * @since 1.0.0
 */
public class ThreadLocalLogUtil {

    private static final ThreadLocal<JobContext> JOB_CONTEXT_LOCAL = new ThreadLocal<>();

    public static void setContext(JobContext jobContext) {
        JOB_CONTEXT_LOCAL.set(jobContext);
    }

    public static JobContext getContext() {
        return JOB_CONTEXT_LOCAL.get();
    }

    public static void removeContext() {
        JOB_CONTEXT_LOCAL.remove();
    }
}
