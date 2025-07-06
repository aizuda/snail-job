package com.aizuda.snailjob.client.job.core.handler;

import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.client.common.rpc.openapi.AbstractRequestHandler;
import com.aizuda.snailjob.client.job.core.openapi.JobOpenApiClient;
import com.aizuda.snailjob.client.job.core.openapi.JobOpenApiClientV2;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.model.SnailJobOpenApiResult;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;

/**
 * @author opensnail
 * @date 2024-09-29 20:40:10
 * @since sj_1.1.0
 */
public abstract class AbstractJobRequestHandler<R> extends AbstractRequestHandler<R> {

    @Deprecated(since = "1.7.0")
    protected JobOpenApiClient client = RequestBuilder.<JobOpenApiClient, SnailJobRpcResult>newBuilder()
            .client(JobOpenApiClient.class)
            .async(false)
            // 走openapi模式
            .openapi(false)
            .build();

    protected JobOpenApiClientV2 clientV2 = RequestBuilder.<JobOpenApiClientV2, SnailJobOpenApiResult>newBuilder()
            .client(JobOpenApiClientV2.class)
            .async(false)
            // 走openapi模式
            .openapi(true)
            .build();

    protected boolean isOpenApiV2() {
        SnailJobProperties properties = SnailSpringContext.getBean(SnailJobProperties.class);
        return properties.isOpenapiV2();
    }
}
