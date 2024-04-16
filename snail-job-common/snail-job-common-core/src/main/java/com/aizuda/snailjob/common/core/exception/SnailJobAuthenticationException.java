package com.aizuda.snailjob.common.core.exception;

import lombok.Getter;

/**
 * @author: opensnail
 * @date : 2024-04-16
 * @since : 1.0.0
 */
@Getter
public class SnailJobAuthenticationException extends BaseSnailJobException {
    private final Integer errorCode = 5001;

    public SnailJobAuthenticationException(final String message) {
        super(message);
    }

    public SnailJobAuthenticationException(String message, Object... arguments) {
        super(message, arguments);
    }

    public SnailJobAuthenticationException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public SnailJobAuthenticationException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public SnailJobAuthenticationException(String message, Object argument) {
        super(message, argument);
    }
}
