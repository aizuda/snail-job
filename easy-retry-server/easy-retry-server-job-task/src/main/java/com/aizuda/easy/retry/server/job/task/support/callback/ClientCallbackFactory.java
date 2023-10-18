package com.aizuda.easy.retry.server.job.task.support.callback;

import com.aizuda.easy.retry.common.core.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.job.task.support.ClientCallbackHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 13:04:09
 * @since 2.4.0
 */
public class ClientCallbackFactory {

    private static final ConcurrentHashMap<TaskTypeEnum, ClientCallbackHandler> CACHE = new ConcurrentHashMap<>();

    public static void registerJobExecutor(TaskTypeEnum taskInstanceType, ClientCallbackHandler callbackHandler) {
        CACHE.put(taskInstanceType, callbackHandler);
    }

    public static ClientCallbackHandler getClientCallback(Integer type) {
        return CACHE.get(TaskTypeEnum.valueOf(type));
    }
}
