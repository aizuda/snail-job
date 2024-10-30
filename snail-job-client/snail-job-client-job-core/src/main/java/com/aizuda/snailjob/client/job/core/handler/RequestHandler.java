package com.aizuda.snailjob.client.job.core.handler;

import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.client.job.core.openapi.OpenApiClient;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;

public interface RequestHandler<R> {

    OpenApiClient client = RequestBuilder.<OpenApiClient, SnailJobRpcResult>newBuilder()
            .client(OpenApiClient.class)
            .async(false)
            .build();

    R execute();

}