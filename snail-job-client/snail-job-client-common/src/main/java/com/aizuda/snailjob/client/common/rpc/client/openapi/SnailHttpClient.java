package com.aizuda.snailjob.client.common.rpc.client.openapi;

import com.aizuda.snailjob.common.core.model.SnailJobOpenApiResult;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-05
 */
public interface SnailHttpClient {

    /**
     * 执行请求
     *
     * @param request 请求参数
     * @return 返回结果
     */
    SnailJobOpenApiResult execute(Request request);

}
