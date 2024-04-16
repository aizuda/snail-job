package com.aizuda.snailjob.common.core.exception;


/**
 * 异常信息
 *
 * @author: opensnail
 * @date : 2021-11-19 15:01
 */
public class SnailJobCommonException extends BaseSnailJobException {

    public SnailJobCommonException(String message) {
        super(message);
    }

    public SnailJobCommonException(String message, Object... arguments) {
        super(message, arguments);
    }

    public SnailJobCommonException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public SnailJobCommonException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public SnailJobCommonException(String message, Object argument) {
        super(message, argument);
    }
}
