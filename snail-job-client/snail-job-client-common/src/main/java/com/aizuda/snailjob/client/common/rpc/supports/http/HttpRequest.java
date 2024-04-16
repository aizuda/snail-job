package com.aizuda.snailjob.client.common.rpc.supports.http;

import io.netty.handler.codec.http.HttpHeaders;
import lombok.Data;

/**
 * @author: opensnail
 * @date : 2024-04-12
 * @since : 3.3.0
 */
@Data
public class HttpRequest {
    private final HttpHeaders headers;
    private final String uri;

    public HttpRequest(HttpHeaders headers, String uri) {
        this.headers = headers;
        this.uri = uri;
    }
}
