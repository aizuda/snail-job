package com.aizuda.snailjob.client.core.handler;

import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.client.common.rpc.openapi.AbstractRequestHandler;
import com.aizuda.snailjob.client.core.openapi.RetryOpenApiClient;
import com.aizuda.snailjob.client.core.openapi.RetryOpenApiClientV2;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.model.SnailJobOpenApiResult;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;

public abstract class AbstractRetryRequestHandler<T> extends AbstractRequestHandler<T> {
    RetryOpenApiClient client = RequestBuilder.<RetryOpenApiClient, SnailJobRpcResult>newBuilder()
            .client(RetryOpenApiClient.class)
            .async(false)
            .openapi(false)
            .build();

    RetryOpenApiClientV2 clientV2 = RequestBuilder.<RetryOpenApiClientV2, SnailJobOpenApiResult>newBuilder()
            .client(RetryOpenApiClientV2.class)
            .async(false)
            .openapi(true)
            .build();

    protected boolean isOpenApiV2() {
        SnailJobProperties properties = SnailSpringContext.getBean(SnailJobProperties.class);
        return properties.isOpenapiV2();
    }
}
