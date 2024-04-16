package com.aizuda.snailjob.client.job.core.cache;

import com.aizuda.snailjob.client.job.core.dto.JobExecutorInfo;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: opensnail
 * @date : 2022-03-03 17:43
 * @since : 2.4.0
 */
public class JobExecutorInfoCache {

    private JobExecutorInfoCache() {}

    private static final ConcurrentHashMap<String, JobExecutorInfo> JOB_EXECUTOR_REPOSITORY = new ConcurrentHashMap<>();

    public static void put(JobExecutorInfo jobExecutorInfo) {
        JOB_EXECUTOR_REPOSITORY.put(jobExecutorInfo.getExecutorName(), jobExecutorInfo);
    }

    public static JobExecutorInfo get(String executorName) {
        return JOB_EXECUTOR_REPOSITORY.get(executorName);
    }

    public static boolean isExisted(String executorName) {
        return Objects.nonNull(JOB_EXECUTOR_REPOSITORY.get(executorName));
    }

}
