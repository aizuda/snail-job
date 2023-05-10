package com.aizuda.easy.retry.client.core.client.netty;

/**
 * 请求类型
 *
 * @author: www.byteblogs.com
 * @date : 2023-05-12 08:53
 * @since 1.3.0
 */
public enum RequestMethod {
    GET,
    HEAD,
    POST,
    PUT,
    PATCH,
    DELETE,
    OPTIONS,
    TRACE;

    private RequestMethod() {
    }
}
