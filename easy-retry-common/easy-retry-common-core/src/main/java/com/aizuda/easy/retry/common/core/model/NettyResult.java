package com.aizuda.easy.retry.common.core.model;

import lombok.Data;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-16 14:07
 */
@Data
public class NettyResult extends Result<Object> {

    private String requestId;

    public NettyResult(int status, String message, Object data, String requestId) {
        super(status, message, data);
        this.requestId = requestId;
    }

    public NettyResult() {
    }

    public NettyResult(Object data, String requestId) {
        super(data);
        this.requestId = requestId;
    }
}
