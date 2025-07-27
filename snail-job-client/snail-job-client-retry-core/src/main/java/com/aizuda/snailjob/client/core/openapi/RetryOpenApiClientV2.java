package com.aizuda.snailjob.client.core.openapi;

import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.annotation.Param;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.client.core.dto.RequestTriggerRetryDTO;
import com.aizuda.snailjob.client.core.dto.RequestUpdateRetryStatusDTO;
import com.aizuda.snailjob.common.core.model.Result;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.*;

public interface RetryOpenApiClientV2 {
    @Mapping(method = RequestMethod.GET, path = OPENAPI_QUERY_RETRY)
    Result<Object> queryRetryTask(@Param("id") Long retryId);

    @Mapping(method = RequestMethod.PUT, path = OPENAPI_TRIGGER_RETRY_V2)
    Result<Object> triggerRetryTask(RequestTriggerRetryDTO triggerRetryDTO);

    @Mapping(method = RequestMethod.PUT, path = OPENAPI_UPDATE_RETRY_STATUS_V2)
    Result<Object> updateRetryTaskStatus(RequestUpdateRetryStatusDTO statusDTO);
}
