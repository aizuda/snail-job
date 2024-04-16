package com.aizuda.snailjob.client.common.exception;

import com.aizuda.snailjob.common.core.exception.BaseSnailJobException;

/**
 * @author: opensnail
 * @date : 2022-03-03 14:49
 */
public class SnailJobClientException extends BaseSnailJobException {

    public SnailJobClientException(String message) {
        super(message);
    }

    public SnailJobClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public SnailJobClientException(Throwable cause) {
        super(cause);
    }

    public SnailJobClientException(String message, Object... arguments) {
        super(message, arguments);
    }

    public SnailJobClientException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public SnailJobClientException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public SnailJobClientException(String message, Object argument) {
        super(message, argument);
    }
}
