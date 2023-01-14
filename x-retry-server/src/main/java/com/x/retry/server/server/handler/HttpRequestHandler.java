package com.x.retry.server.server.handler;

import cn.hutool.core.net.url.UrlBuilder;
import io.netty.handler.codec.http.HttpMethod;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 16:23
 */
public interface HttpRequestHandler {

    boolean supports(String uri);

    HttpMethod method();

    String doHandler(String content, UrlBuilder urlBuilder);

}
