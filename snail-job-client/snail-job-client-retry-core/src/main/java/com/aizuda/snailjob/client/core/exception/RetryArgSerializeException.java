package com.aizuda.snailjob.client.core.exception;

import com.aizuda.snailjob.common.core.exception.BaseSnailJobException;

public class RetryArgSerializeException extends BaseSnailJobException {
    public RetryArgSerializeException(String message) {
        super(message);
    }

    public RetryArgSerializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RetryArgSerializeException(Throwable cause) {
        super(cause);
    }

    public RetryArgSerializeException(String message, Object... arguments) {
        super(message, arguments);
    }

    public RetryArgSerializeException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public RetryArgSerializeException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public RetryArgSerializeException(String message, Object argument) {
        super(message, argument);
    }
}
