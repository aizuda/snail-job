package com.aizuda.easy.retry.common.core.exception;


/**
 * 异常信息
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-19 15:01
 */
public class EasyRetryCommonException extends BaseEasyRetryException {

    public EasyRetryCommonException(String message) {
        super(message);
    }

    public EasyRetryCommonException(String message, Object... arguments) {
        super(message, arguments);
    }

    public EasyRetryCommonException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public EasyRetryCommonException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public EasyRetryCommonException(String message, Object argument) {
        super(message, argument);
    }
}
