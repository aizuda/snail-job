package com.x.retry.client.core.cache;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.x.retry.client.core.retryer.RetryerInfo;
import org.springframework.stereotype.Component;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 17:43
 */
public class RetryerInfoCache {

    private static Table<String, String, RetryerInfo> RETRY_HANDLER_REPOSITORY = HashBasedTable.create();

    public static RetryerInfo put(RetryerInfo retryerInfo) {
       return RETRY_HANDLER_REPOSITORY.put(retryerInfo.getScene(), retryerInfo.getExecutorClassName(), retryerInfo);
    }

    public static RetryerInfo get(String sceneName, String executorClassName) {
        return RETRY_HANDLER_REPOSITORY.get(sceneName, executorClassName);
    }

}
