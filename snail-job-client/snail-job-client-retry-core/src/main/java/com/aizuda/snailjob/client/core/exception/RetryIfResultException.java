package com.aizuda.snailjob.client.core.exception;

import com.aizuda.snailjob.common.core.exception.BaseSnailJobException;

public class RetryIfResultException extends BaseSnailJobException {
    public RetryIfResultException(String message) {
        super(message);
    }

    public RetryIfResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public RetryIfResultException(Throwable cause) {
        super(cause);
    }

    public RetryIfResultException(String message, Object... arguments) {
        super(message, arguments);
    }

    public RetryIfResultException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public RetryIfResultException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public RetryIfResultException(String message, Object argument) {
        super(message, argument);
    }
}
