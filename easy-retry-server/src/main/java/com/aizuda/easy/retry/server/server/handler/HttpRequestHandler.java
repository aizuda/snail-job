package com.aizuda.easy.retry.server.server.handler;

import cn.hutool.core.net.url.UrlBuilder;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

/**
 * 处理http请求
 *
 * @author: www.byteblogs.com
 * @date : 2022-03-07 16:23
 * @since 1.0.0
 */
public interface HttpRequestHandler {

    boolean supports(String path);

    HttpMethod method();

    String doHandler(String content, UrlBuilder urlBuilder, HttpHeaders headers);

}
