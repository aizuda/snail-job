package com.aizuda.snailjob.client.job.core.handler;

import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.client.common.rpc.openapi.AbstractRequestHandler;
import com.aizuda.snailjob.client.job.core.openapi.JobOpenApiClient;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;

/**
 * @author opensnail
 * @date 2024-09-29 20:40:10
 * @since sj_1.1.0
 */
public abstract class AbstractJobRequestHandler<R> extends AbstractRequestHandler<R> {
    protected JobOpenApiClient client = RequestBuilder.<JobOpenApiClient, SnailJobRpcResult>newBuilder()
            .client(JobOpenApiClient.class)
            .async(false)
            .build();
}
