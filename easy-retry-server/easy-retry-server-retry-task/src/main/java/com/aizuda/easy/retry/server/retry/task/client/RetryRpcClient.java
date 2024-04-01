package com.aizuda.easy.retry.server.retry.task.client;

import com.aizuda.easy.retry.client.model.DispatchRetryDTO;
import com.aizuda.easy.retry.client.model.DispatchRetryResultDTO;
import com.aizuda.easy.retry.client.model.GenerateRetryIdempotentIdDTO;
import com.aizuda.easy.retry.client.model.RetryCallbackDTO;
import com.aizuda.easy.retry.client.model.StopJobDTO;
import com.aizuda.easy.retry.client.model.request.DispatchJobRequest;
import com.aizuda.easy.retry.common.core.model.EasyRetryHeaders;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.common.client.RequestMethod;
import com.aizuda.easy.retry.server.common.client.annotation.Body;
import com.aizuda.easy.retry.server.common.client.annotation.Header;
import com.aizuda.easy.retry.server.common.client.annotation.Mapping;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO;

/**
 * 调用客户端接口
 *
 * @author: www.byteblogs.com
 * @date : 2023-06-19 15:40
 * @since 2.0.0
 */
public interface RetryRpcClient {

    @Mapping(path = "/retry/dispatch/v1", method = RequestMethod.POST)
    Result<DispatchRetryResultDTO> dispatch(@Body DispatchRetryDTO dispatchRetryDTO, @Header EasyRetryHeaders headers);

    @Mapping(path = "/retry/callback/v1", method = RequestMethod.POST)
    Result callback(@Body RetryCallbackDTO retryCallbackDTO);

    @Mapping(path = "/retry/generate/idempotent-id/v1", method = RequestMethod.POST)
    Result generateIdempotentId(@Body GenerateRetryIdempotentIdDTO retryCallbackDTO);

    @Mapping(path = "/retry/sync/version/v1", method = RequestMethod.POST)
    Result syncConfig(@Body ConfigDTO configDTO);

}
