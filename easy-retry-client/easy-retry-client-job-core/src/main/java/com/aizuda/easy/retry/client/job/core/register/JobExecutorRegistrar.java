package com.aizuda.easy.retry.client.job.core.register;

import com.aizuda.easy.retry.client.common.Lifecycle;
import com.aizuda.easy.retry.client.common.exception.EasyRetryClientException;
import com.aizuda.easy.retry.client.job.core.Scanner;
import com.aizuda.easy.retry.client.job.core.cache.JobExecutorInfoCache;
import com.aizuda.easy.retry.client.job.core.dto.JobExecutorInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author: www.byteblogs.com
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
