package com.aizuda.snail.job.server.retry.task.client;

import com.aizuda.snail.job.client.model.DispatchRetryDTO;
import com.aizuda.snail.job.client.model.DispatchRetryResultDTO;
import com.aizuda.snail.job.client.model.GenerateRetryIdempotentIdDTO;
import com.aizuda.snail.job.client.model.RetryCallbackDTO;
import com.aizuda.snail.job.common.core.model.EasyRetryHeaders;
import com.aizuda.snail.job.common.core.model.Result;
import com.aizuda.snail.job.server.common.rpc.client.RequestMethod;
import com.aizuda.snail.job.server.common.rpc.client.annotation.Body;
import com.aizuda.snail.job.server.common.rpc.client.annotation.Header;
import com.aizuda.snail.job.server.common.rpc.client.annotation.Mapping;
import com.aizuda.snail.job.server.model.dto.ConfigDTO;

/**
 * 调用客户端接口
 *
 * @author: opensnail
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
