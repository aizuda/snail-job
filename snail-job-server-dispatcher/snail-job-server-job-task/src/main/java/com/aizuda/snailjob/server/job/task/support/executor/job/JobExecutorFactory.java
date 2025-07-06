package com.aizuda.snailjob.server.job.task.support.executor.job;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.server.job.task.support.JobExecutor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author opensnail
 * @date 2023-10-02 13:04:09
 * @since 2.4.0
 */
public class JobExecutorFactory {

    private static final ConcurrentHashMap<JobTaskTypeEnum, JobExecutor> CACHE = new ConcurrentHashMap<>();

    public static void registerJobExecutor(JobTaskTypeEnum taskInstanceType, JobExecutor executor) {
        CACHE.put(taskInstanceType, executor);
    }

    public static JobExecutor getJobExecutor(Integer type) {
        return CACHE.get(JobTaskTypeEnum.valueOf(type));
    }
}
