package com.aizuda.snailjob.server.retry.task.client;

import com.aizuda.snailjob.client.model.DispatchRetryResultDTO;
import com.aizuda.snailjob.client.model.GenerateRetryIdempotentIdDTO;
import com.aizuda.snailjob.client.model.RetryCallbackDTO;
import com.aizuda.snailjob.client.model.request.DispatchRetryRequest;
import com.aizuda.snailjob.client.model.request.StopRetryRequest;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.model.SnailJobHeaders;
import com.aizuda.snailjob.server.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Body;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Header;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Mapping;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.*;

/**
 * 调用客户端接口
 *
 * @author: opensnail
 * @date : 2023-06-19 15:40
 * @since 2.0.0
 */
public interface RetryRpcClient {

    @Mapping(path = RETRY_DISPATCH, method = RequestMethod.POST)
    Result<Boolean> dispatch(@Body DispatchRetryRequest dispatchRetryRequest, @Header SnailJobHeaders headers);

    @Mapping(path = RETRY_STOP, method = RequestMethod.POST)
    Result<Boolean> stop(@Body StopRetryRequest stopRetryRequest);

    @Mapping(path = RETRY_CALLBACK, method = RequestMethod.POST)
    Result callback(@Body RetryCallbackDTO retryCallbackDTO);

    @Mapping(path = RETRY_GENERATE_IDEM_ID, method = RequestMethod.POST)
    Result generateIdempotentId(@Body GenerateRetryIdempotentIdDTO retryCallbackDTO);

}
