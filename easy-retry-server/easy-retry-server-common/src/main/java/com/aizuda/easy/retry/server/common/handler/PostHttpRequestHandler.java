package com.aizuda.easy.retry.server.common.handler;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.easy.retry.server.common.HttpRequestHandler;
import io.netty.handler.codec.http.HttpHeaders;

/**
 * 处理POST请求
 *
 * @author: www.byteblogs.com
 * @date : 2022-03-07 17:35
 * @since 1.0.0
 */
public abstract class PostHttpRequestHandler implements HttpRequestHandler {

    @Override
    public String doHandler(String content, UrlBuilder builder, HttpHeaders headers) {
        UrlQuery query = builder.getQuery();

        return doHandler(content, query, headers);
    }

    public abstract String doHandler(String content, UrlQuery query, HttpHeaders headers);
}
