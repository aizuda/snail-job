package com.aizuda.easy.retry.server.job.task.support.executor;

import com.aizuda.easy.retry.server.job.task.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.job.task.support.JobExecutor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 13:04:09
 * @since 2.4.0
 */
public class JobExecutorFactory {

    private static final ConcurrentHashMap<TaskTypeEnum, JobExecutor> CACHE = new ConcurrentHashMap<>();

    public static void registerJobExecutor(TaskTypeEnum taskInstanceType, JobExecutor executor) {
        CACHE.put(taskInstanceType, executor);
    }

    public static JobExecutor getJobExecutor(Integer type) {
        return CACHE.get(TaskTypeEnum.valueOf(type));
    }
}
