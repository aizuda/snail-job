package com.aizuda.snailjob.server.common;

import cn.hutool.core.net.url.UrlBuilder;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

/**
 * 处理http请求
 *
 * @author: opensnail
 * @date : 2022-03-07 16:23
 * @since 1.0.0
 */
public interface HttpRequestHandler {

    boolean supports(String path);

    HttpMethod method();

    SnailJobRpcResult doHandler(String content, UrlBuilder urlBuilder, HttpHeaders headers);

}
