package com.aizuda.snailjob.server.common.handler;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.server.common.HttpRequestHandler;
import io.netty.handler.codec.http.HttpHeaders;

/**
 * 处理GRT请求
 *
 * @author: opensnail
 * @date : 2022-03-07 17:35
 * @since 1.0.0
 */
public abstract class GetHttpRequestHandler implements HttpRequestHandler {

    @Override
    public SnailJobRpcResult doHandler(String content, UrlBuilder builder, HttpHeaders headers) {
        UrlQuery query = builder.getQuery();

        return doHandler(content, query, headers);
    }

    public abstract SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers);
}
