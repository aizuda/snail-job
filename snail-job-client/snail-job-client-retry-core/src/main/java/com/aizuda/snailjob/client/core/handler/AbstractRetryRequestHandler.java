package com.aizuda.snailjob.client.core.handler;

import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.client.common.rpc.openapi.AbstractRequestHandler;
import com.aizuda.snailjob.client.core.openapi.RetryOpenApiClient;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;

public abstract class AbstractRetryRequestHandler<T> extends AbstractRequestHandler<T> {
    RetryOpenApiClient client = RequestBuilder.<RetryOpenApiClient, SnailJobRpcResult>newBuilder()
            .client(RetryOpenApiClient.class)
            .async(false)
            .build();
}
