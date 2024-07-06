package com.aizuda.snailjob.common.core.model;

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

    private long reqId;

    public NettyResult(int status, String message, Object data, long reqId) {
        super(status, message, data);
        this.reqId = reqId;
    }

    public NettyResult() {
    }

    public NettyResult(Object data, long reqId) {
        super(data);
        this.reqId = reqId;
    }
}
