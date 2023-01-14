package com.x.retry.server.server.handler;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 17:35
 */
public abstract class PostHttpRequestHandler implements HttpRequestHandler {

    @Override
    public String doHandler(String content,  UrlBuilder builder) {
        UrlQuery query = builder.getQuery();
        return doHandler(content, query);
    }

    public abstract String doHandler(String content, UrlQuery query);
}
