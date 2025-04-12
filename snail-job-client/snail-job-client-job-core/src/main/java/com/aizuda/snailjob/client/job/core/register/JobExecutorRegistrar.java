package com.aizuda.snailjob.client.job.core.register;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.Scanner;
import com.aizuda.snailjob.client.job.core.cache.JobExecutorInfoCache;
import com.aizuda.snailjob.client.job.core.dto.JobExecutorInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-02-10 09:12
 */
@Component
@RequiredArgsConstructor
public class JobExecutorRegistrar implements Lifecycle {
    private final List<Scanner> scanners;

    public void registerRetryHandler(JobExecutorInfo jobExecutorInfo) {

        if (JobExecutorInfoCache.isExisted(jobExecutorInfo.getExecutorName())) {
            throw new SnailJobClientException("Duplicate executor names are not allowed: {}", jobExecutorInfo.getExecutorName());
        }

        JobExecutorInfoCache.put(jobExecutorInfo);
    }

    public void registerRetryHandler(List<JobExecutorInfo> contextList) {
        for (JobExecutorInfo jobExecutorInfo : contextList) {
            registerRetryHandler(jobExecutorInfo);
        }
    }

    @Override
    public void start() {
        for (Scanner scanner : scanners) {
            this.registerRetryHandler(scanner.doScan());
        }
    }

    @Override
    public void close() {
    }
}
