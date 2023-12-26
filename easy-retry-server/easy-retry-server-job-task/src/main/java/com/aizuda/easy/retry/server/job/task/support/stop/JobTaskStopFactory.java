package com.aizuda.easy.retry.server.job.task.support.stop;

import com.aizuda.easy.retry.common.core.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.job.task.support.JobTaskStopHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 13:04:09
 * @since 2.4.0
 */
public final class JobTaskStopFactory {

    private static final ConcurrentHashMap<TaskTypeEnum, JobTaskStopHandler> CACHE = new ConcurrentHashMap<>();

    private JobTaskStopFactory() {
    }

    public static void registerTaskStop(TaskTypeEnum taskInstanceType, JobTaskStopHandler interrupt) {
        CACHE.put(taskInstanceType, interrupt);
    }

    public static JobTaskStopHandler getJobTaskStop(Integer type) {
        return CACHE.get(TaskTypeEnum.valueOf(type));
    }

    public static JobTaskStopFactory createJobTaskStopFactory() {
        return new JobTaskStopFactory();
    }
}
