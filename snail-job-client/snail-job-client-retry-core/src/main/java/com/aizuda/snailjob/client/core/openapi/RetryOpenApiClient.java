package com.aizuda.snailjob.client.core.openapi;

import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.model.request.base.StatusUpdateRequest;
import com.aizuda.snailjob.model.request.TriggerRetryApiRequest;

@Deprecated(since = "1.7.0")
public interface RetryOpenApiClient {
    @Mapping(method = RequestMethod.POST, path = "/api/retry/query")
    Result<Object> queryRetryTask(Long retryId);

    @Mapping(method = RequestMethod.POST, path = "/api/retry/triggerRetry")
    Result<Object> triggerRetryTask(TriggerRetryApiRequest triggerRetryDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/retry/updateRetryStatus")
    Result<Object> updateRetryTaskStatus(StatusUpdateRequest statusDTO);
}
