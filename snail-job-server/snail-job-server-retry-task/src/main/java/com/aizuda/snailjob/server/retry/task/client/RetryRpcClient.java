package com.aizuda.snailjob.server.retry.task.client;

import com.aizuda.snailjob.client.model.DispatchRetryDTO;
import com.aizuda.snailjob.client.model.DispatchRetryResultDTO;
import com.aizuda.snailjob.client.model.GenerateRetryIdempotentIdDTO;
import com.aizuda.snailjob.client.model.RetryCallbackDTO;
import com.aizuda.snailjob.common.core.model.SnailJobHeaders;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.server.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Body;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Header;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Mapping;
import com.aizuda.snailjob.server.model.dto.ConfigDTO;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Body;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Header;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Mapping;

/**
 * 调用客户端接口
 *
 * @author: opensnail
 * @date : 2023-06-19 15:40
 * @since 2.0.0
 */
public interface RetryRpcClient {

    @Mapping(path = "/retry/dispatch/v1", method = RequestMethod.POST)
    Result<DispatchRetryResultDTO> dispatch(@Body DispatchRetryDTO dispatchRetryDTO, @Header SnailJobHeaders headers);

    @Mapping(path = "/retry/callback/v1", method = RequestMethod.POST)
    Result callback(@Body RetryCallbackDTO retryCallbackDTO);

    @Mapping(path = "/retry/generate/idempotent-id/v1", method = RequestMethod.POST)
    Result generateIdempotentId(@Body GenerateRetryIdempotentIdDTO retryCallbackDTO);

    @Mapping(path = "/retry/sync/version/v1", method = RequestMethod.POST)
    Result syncConfig(@Body ConfigDTO configDTO);

}
