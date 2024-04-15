package com.aizuda.snail.job.client.job.core.client;

import com.aizuda.snail.job.client.common.annotation.Mapping;
import com.aizuda.snail.job.client.common.rpc.client.RequestMethod;
import com.aizuda.snail.job.client.model.request.DispatchJobResultRequest;
import com.aizuda.snail.job.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snail.job.common.core.model.Result;

/**
 * netty 客户端请求类
 *
 * @author: opensnail
 * @date : 2023-05-11 21:28
 * @since 2.4.0
 */
public interface JobNettyClient {

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.REPORT_JOB_DISPATCH_RESULT)
    Result dispatchResult(DispatchJobResultRequest request);

}
