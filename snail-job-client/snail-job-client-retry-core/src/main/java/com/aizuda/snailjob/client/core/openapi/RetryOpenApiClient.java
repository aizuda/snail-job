package com.aizuda.snailjob.client.core.openapi;

import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.client.core.dto.RequestTriggerRetryDTO;
import com.aizuda.snailjob.client.core.dto.RequestUpdateRetryStatusDTO;
import com.aizuda.snailjob.common.core.model.Result;

@Deprecated(since = "1.8.0")
public interface RetryOpenApiClient {
    @Mapping(method = RequestMethod.POST, path = "/api/retry/query")
    Result<Object> queryRetryTask(Long retryId);

    @Mapping(method = RequestMethod.POST, path = "/api/retry/triggerRetry")
    Result<Object> triggerRetryTask(RequestTriggerRetryDTO triggerRetryDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/retry/updateRetryStatus")
    Result<Object> updateRetryTaskStatus(RequestUpdateRetryStatusDTO statusDTO);
}
