package com.aizuda.easy.retry.server.job.task.client;

import com.aizuda.easy.retry.client.model.StopJobDTO;
import com.aizuda.easy.retry.client.model.request.DispatchJobRequest;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.common.client.RequestMethod;
import com.aizuda.easy.retry.server.common.client.annotation.Body;
import com.aizuda.easy.retry.server.common.client.annotation.Mapping;

/**
 * 调用客户端接口
 *
 * @author: www.byteblogs.com
 * @date : 2023-06-19 15:40
 * @since 2.0.0
 */
public interface JobRpcClient {

    @Mapping(path = "/job/stop/v1", method = RequestMethod.POST)
    Result<Boolean> stop(@Body StopJobDTO stopJobDTO);

    @Mapping(path = "/job/dispatch/v1", method = RequestMethod.POST)
    Result<Boolean> dispatch(@Body DispatchJobRequest dispatchJobRequest);

}
