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
public class SnailJobRpcResult extends Result<Object> {

    private long reqId;

    public SnailJobRpcResult(int status, String message, Object data, long reqId) {
        super(status, message, data);
        this.reqId = reqId;
    }

    public SnailJobRpcResult() {
    }

    public SnailJobRpcResult(Object data, long reqId) {
        super(data);
        this.reqId = reqId;
    }
}
