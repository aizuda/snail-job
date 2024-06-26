package com.aizuda.snailjob.client.job.core.executor;

import com.aizuda.snailjob.common.core.model.JobContext;

/**
 * @author: opensnail
 * @date : 2024-06-13
 * @since : sj_1.1.0
 */
public final class JobContextManager {

    private static final ThreadLocal<JobContext> JOB_CONTEXT_LOCAL = new ThreadLocal<>();

    public static void setJobContext(JobContext jobContext) {
        JOB_CONTEXT_LOCAL.set(jobContext);
    }

    public static JobContext getJobContext() {
        return JOB_CONTEXT_LOCAL.get();
    }

    public static void removeJobContext() {
        JOB_CONTEXT_LOCAL.remove();
    }
}
