package com.aizuda.easy.retry.client.common.netty.server;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: opensnail
 * @date : 2024-04-12
 * @since :3.3.0
 */
@Data
public class HttpResponse {

    private Map<String, Object> headers = new HashMap<>();

    public void setHeader(String key, Object value) {
        headers.put(key, value);
    }
}
