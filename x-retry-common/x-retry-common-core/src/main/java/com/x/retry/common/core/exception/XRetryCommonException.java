package com.x.retry.common.core.exception;


/**
 * 异常信息
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-19 15:01
 */
public class XRetryCommonException extends BaseXRetryException {

    public XRetryCommonException(String message) {
        super(message);
    }

    public XRetryCommonException(String message, Object... arguments) {
        super(message, arguments);
    }

    public XRetryCommonException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public XRetryCommonException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public XRetryCommonException(String message, Object argument) {
        super(message, argument);
    }
}
