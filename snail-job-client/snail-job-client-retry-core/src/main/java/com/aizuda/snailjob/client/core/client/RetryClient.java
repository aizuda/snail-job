package com.aizuda.snailjob.client.core.client;

import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.client.model.request.DispatchJobResultRequest;
import com.aizuda.snailjob.client.model.request.DispatchRetryResultRequest;
import com.aizuda.snailjob.client.model.request.MapTaskRequest;
import com.aizuda.snailjob.client.model.request.RetryCallbackResultRequest;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.model.Result;

/**
 * netty 客户端请求类
 *
 * @author: opensnail
 * @date : 2023-05-11 21:28
 * @since 2.4.0
 */
public interface RetryClient {

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.REPORT_RETRY_DISPATCH_RESULT)
    Result dispatchResult(DispatchRetryResultRequest request);

}
