package com.aizuda.snail.job.server.job.task.client;

import com.aizuda.snail.job.client.model.StopJobDTO;
import com.aizuda.snail.job.client.model.request.DispatchJobRequest;
import com.aizuda.snail.job.common.core.model.Result;
import com.aizuda.snail.job.server.common.rpc.client.RequestMethod;
import com.aizuda.snail.job.server.common.rpc.client.annotation.Body;
import com.aizuda.snail.job.server.common.rpc.client.annotation.Mapping;

/**
 * 调用客户端接口
 *
 * @author: opensnail
 * @date : 2023-06-19 15:40
 * @since 2.0.0
 */
public interface JobRpcClient {

    @Mapping(path = "/job/stop/v1", method = RequestMethod.POST)
    Result<Boolean> stop(@Body StopJobDTO stopJobDTO);

    @Mapping(path = "/job/dispatch/v1", method = RequestMethod.POST)
    Result<Boolean> dispatch(@Body DispatchJobRequest dispatchJobRequest);

}
