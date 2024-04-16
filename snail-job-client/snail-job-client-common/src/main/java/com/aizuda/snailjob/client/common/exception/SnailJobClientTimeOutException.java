package com.aizuda.snailjob.client.common.exception;

import com.aizuda.snailjob.common.core.exception.BaseSnailJobException;

/**
 * @author: opensnail
 * @date : 2022-03-03 14:49
 */
public class SnailJobClientTimeOutException extends BaseSnailJobException {

    public SnailJobClientTimeOutException(String message) {
        super(message);
    }

    public SnailJobClientTimeOutException(String message, Throwable cause) {
        super(message, cause);
    }

    public SnailJobClientTimeOutException(Throwable cause) {
        super(cause);
    }

    public SnailJobClientTimeOutException(String message, Object... arguments) {
        super(message, arguments);
    }

    public SnailJobClientTimeOutException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public SnailJobClientTimeOutException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public SnailJobClientTimeOutException(String message, Object argument) {
        super(message, argument);
    }
}
