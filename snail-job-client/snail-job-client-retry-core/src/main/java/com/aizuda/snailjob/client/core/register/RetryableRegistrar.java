package com.aizuda.snailjob.client.core.register;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.core.Scanner;
import com.aizuda.snailjob.client.core.cache.RetryerInfoCache;
import com.aizuda.snailjob.client.core.exception.SnailJobClientException;
import com.aizuda.snailjob.client.core.retryer.RetryerInfo;
import com.aizuda.snailjob.client.core.exception.SnailJobClientException;
import com.aizuda.snailjob.client.core.retryer.RetryerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author: opensnail
 * @date : 2022-02-10 09:12
 */
@Component
public class RetryableRegistrar implements Lifecycle {

    @Autowired
    private List<Scanner> scanners;

    public void registerRetryHandler(RetryerInfo retryerInfo) {

        if (Objects.nonNull(RetryerInfoCache.get(retryerInfo.getScene(), retryerInfo.getExecutorClassName()))) {
            throw new SnailJobClientException("类:[{}]中已经存在场景:[{}]",  retryerInfo.getExecutorClassName(), retryerInfo.getScene());
        }

        RetryerInfoCache.put(retryerInfo);
    }

    public void registerRetryHandler(List<RetryerInfo> contextList) {
        for (RetryerInfo retryerInfo : contextList) {
            registerRetryHandler(retryerInfo);
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
