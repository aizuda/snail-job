package com.aizuda.snailjob.server.job.task.client;

import com.aizuda.snailjob.model.request.StopJobRequest;
import com.aizuda.snailjob.model.request.DispatchJobRequest;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.server.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Body;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Mapping;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.JOB_DISPATCH;
import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.JOB_STOP;

/**
 * 调用客户端接口
 *
 * @author: opensnail
 * @date : 2023-06-19 15:40
 * @since 2.0.0
 */
public interface JobRpcClient {

    @Mapping(path = JOB_STOP, method = RequestMethod.POST)
    Result<Boolean> stop(@Body StopJobRequest stopJobRequest);

    @Mapping(path = JOB_DISPATCH, method = RequestMethod.POST)
    Result<Boolean> dispatch(@Body DispatchJobRequest dispatchJobRequest);

}
