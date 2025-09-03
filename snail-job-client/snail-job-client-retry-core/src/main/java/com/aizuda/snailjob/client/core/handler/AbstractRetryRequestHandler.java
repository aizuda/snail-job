package com.aizuda.snailjob.client.core.handler;

import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.client.common.rpc.openapi.AbstractRequestHandler;
import com.aizuda.snailjob.client.core.openapi.RetryOpenApiClientV2;
import com.aizuda.snailjob.common.core.model.SnailJobOpenApiResult;

public abstract class AbstractRetryRequestHandler<T> extends AbstractRequestHandler<T> {
    RetryOpenApiClientV2 clientV2 = RequestBuilder.<RetryOpenApiClientV2, SnailJobOpenApiResult>newBuilder()
            .client(RetryOpenApiClientV2.class)
            .async(false)
            .openapi(true)
            .build();
}
