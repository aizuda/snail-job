package com.aizuda.snailjob.common.core.exception;

/**
 * @author: opensnail
 * @date : 2022-03-03 14:49
 */
public class SnailJobRemotingTimeOutException extends BaseSnailJobException {

    public SnailJobRemotingTimeOutException(String message) {
        super(message);
    }

    public SnailJobRemotingTimeOutException(String message, Throwable cause) {
        super(message, cause);
    }

    public SnailJobRemotingTimeOutException(Throwable cause) {
        super(cause);
    }

    public SnailJobRemotingTimeOutException(String message, Object... arguments) {
        super(message, arguments);
    }

    public SnailJobRemotingTimeOutException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public SnailJobRemotingTimeOutException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public SnailJobRemotingTimeOutException(String message, Object argument) {
        super(message, argument);
    }
}
