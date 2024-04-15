package com.aizuda.snail.job.common.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author: opensnail
 * @date : 2022-02-16 14:07
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class NettyResult extends Result<Object> {

    private long requestId;

    public NettyResult(int status, String message, Object data, long requestId) {
        super(status, message, data);
        this.requestId = requestId;
    }

    public NettyResult() {
    }

    public NettyResult(Object data, long requestId) {
        super(data);
        this.requestId = requestId;
    }
}
