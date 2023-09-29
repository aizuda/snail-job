package com.aizuda.easy.retry.client.job.core.client;

import com.aizuda.easy.retry.client.common.annotation.Mapping;
import com.aizuda.easy.retry.client.common.netty.RequestMethod;
import com.aizuda.easy.retry.client.model.request.DispatchJobRequest;
import com.aizuda.easy.retry.client.model.request.DispatchJobResultRequest;
import com.aizuda.easy.retry.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.easy.retry.common.core.model.Result;

/**
 * netty 客户端请求类
 *
 * @author: www.byteblogs.com
 * @date : 2023-05-11 21:28
 * @since 2.4.0
 */
public interface JobNettyClient {

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.REPORT_JOB_DISPATCH_RESULT)
    Result dispatchResult(DispatchJobResultRequest request);

}
