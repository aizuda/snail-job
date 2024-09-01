package com.aizuda.snailjob.common.core.exception;

public class SnailJobInnerExecutorException extends BaseSnailJobException{
    public SnailJobInnerExecutorException(String message) {
        super(message);
    }

    public SnailJobInnerExecutorException(String message, Throwable cause) {
        super(message, cause);
    }

    public SnailJobInnerExecutorException(Throwable cause) {
        super(cause);
    }

    public SnailJobInnerExecutorException(String message, Object... arguments) {
        super(message, arguments);
    }

    public SnailJobInnerExecutorException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public SnailJobInnerExecutorException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public SnailJobInnerExecutorException(String message, Object argument) {
        super(message, argument);
    }
}
