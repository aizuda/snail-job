package com.aizuda.snailjob.server.job.task.support.generator.task;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author opensnail
 * @date 2023-10-02 13:04:09
 * @since 2.4.0
 */
public class JobTaskGeneratorFactory {

    private static final ConcurrentHashMap<JobTaskTypeEnum, JobTaskGenerator> CACHE = new ConcurrentHashMap<>();

    public static void registerTaskInstance(JobTaskTypeEnum taskInstanceType, JobTaskGenerator generator) {
        CACHE.put(taskInstanceType, generator);
    }

    public static JobTaskGenerator getTaskInstance(Integer type) {
        return CACHE.get(JobTaskTypeEnum.valueOf(type));
    }
}
