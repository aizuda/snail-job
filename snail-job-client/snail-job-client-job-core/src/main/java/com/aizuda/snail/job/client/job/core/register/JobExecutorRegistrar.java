package com.aizuda.snail.job.client.job.core.register;

import com.aizuda.snail.job.client.common.Lifecycle;
import com.aizuda.snail.job.client.common.exception.EasyRetryClientException;
import com.aizuda.snail.job.client.job.core.Scanner;
import com.aizuda.snail.job.client.job.core.cache.JobExecutorInfoCache;
import com.aizuda.snail.job.client.job.core.dto.JobExecutorInfo;
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
            throw new EasyRetryClientException("不允许executorName重复的",  jobExecutorInfo.getExecutorName());
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
