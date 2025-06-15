package com.aizuda.snailjob.client.core.exception;

import com.aizuda.snailjob.common.core.exception.BaseSnailJobException;

public class RetryConditionException extends BaseSnailJobException {
    public RetryConditionException(String message) {
        super(message);
    }

    public RetryConditionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RetryConditionException(Throwable cause) {
        super(cause);
    }

    public RetryConditionException(String message, Object... arguments) {
        super(message, arguments);
    }

    public RetryConditionException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public RetryConditionException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public RetryConditionException(String message, Object argument) {
        super(message, argument);
    }
}
