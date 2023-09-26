package com.aizuda.easy.retry.server.common.client;

import com.aizuda.easy.retry.client.model.DispatchJobDTO;
import com.aizuda.easy.retry.client.model.DispatchRetryDTO;
import com.aizuda.easy.retry.client.model.DispatchRetryResultDTO;
import com.aizuda.easy.retry.client.model.InterruptJobDTO;
import com.aizuda.easy.retry.client.model.RetryCallbackDTO;
import com.aizuda.easy.retry.common.core.model.EasyRetryHeaders;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.common.client.annotation.Body;
import com.aizuda.easy.retry.server.common.client.annotation.Header;
import com.aizuda.easy.retry.server.common.client.annotation.Mapping;

/**
 * 调用客户端接口
 *
 * @author: www.byteblogs.com
 * @date : 2023-06-19 15:40
 * @since 2.0.0
 */
public interface RpcClient {

    @Mapping(path = "/retry/dispatch/v1", method = RequestMethod.POST)
    Result<DispatchRetryResultDTO> dispatch(@Body DispatchRetryDTO dispatchRetryDTO, @Header EasyRetryHeaders headers);

    @Mapping(path = "/retry/callback/v1", method = RequestMethod.POST)
    Result callback(@Body RetryCallbackDTO retryCallbackDTO);

    @Mapping(path = "/job/interrupt/v1", method = RequestMethod.POST)
    Result<Boolean> interrupt(@Body InterruptJobDTO interruptJobDTO);

    @Mapping(path = "/job/dispatch/v1", method = RequestMethod.POST)
    Result dispatch(@Body DispatchJobDTO dispatchJobDTO);

}
