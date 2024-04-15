package com.aizuda.snail.job.server.common.handler;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snail.job.server.common.HttpRequestHandler;
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
    public String doHandler(String content, UrlBuilder builder, HttpHeaders headers) {
        UrlQuery query = builder.getQuery();

        return doHandler(content, query, headers);
    }

    public abstract String doHandler(String content, UrlQuery query, HttpHeaders headers);
}
