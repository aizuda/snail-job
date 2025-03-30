package com.aizuda.snailjob.client.core.openapi;

import com.aizuda.snailjob.client.core.handler.QueryRetryHandler;
import com.aizuda.snailjob.client.core.handler.TriggerRetryHandler;
import com.aizuda.snailjob.client.core.handler.UpdateRetryStatusHandler;

public class SnailRetryOpenApi {

    public static QueryRetryHandler queryTask(Long retryId) {
        return new QueryRetryHandler(retryId);
    }

    public static UpdateRetryStatusHandler updateTaskStatus(Long retryId) {
        return new UpdateRetryStatusHandler(retryId);
    }

    public static TriggerRetryHandler triggerTask(Long retryId) {
        return new TriggerRetryHandler(retryId);
    }
}
