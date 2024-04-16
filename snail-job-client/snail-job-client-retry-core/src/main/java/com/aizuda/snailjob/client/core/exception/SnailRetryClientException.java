package com.aizuda.snailjob.client.core.exception;

import com.aizuda.snailjob.common.core.exception.BaseSnailJobException;

/**
 * @author: opensnail
 * @date : 2022-03-03 14:49
 */
public class SnailRetryClientException extends BaseSnailJobException {

    public SnailRetryClientException(String message) {
        super(message);
    }

    public SnailRetryClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public SnailRetryClientException(Throwable cause) {
        super(cause);
    }

    public SnailRetryClientException(String message, Object... arguments) {
        super(message, arguments);
    }

    public SnailRetryClientException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public SnailRetryClientException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public SnailRetryClientException(String message, Object argument) {
        super(message, argument);
    }
}
