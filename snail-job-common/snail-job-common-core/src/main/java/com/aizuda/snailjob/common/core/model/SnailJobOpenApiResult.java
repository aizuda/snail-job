package com.aizuda.snailjob.common.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author: opensnail
 * @date : 2025-07-05 14:07
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SnailJobOpenApiResult extends Result<Object> {

    public SnailJobOpenApiResult(int status, String message, Object data) {
        super(status, message, data);
    }

    public SnailJobOpenApiResult() {
    }

    public SnailJobOpenApiResult(Object data) {
        super(data);
    }
}
