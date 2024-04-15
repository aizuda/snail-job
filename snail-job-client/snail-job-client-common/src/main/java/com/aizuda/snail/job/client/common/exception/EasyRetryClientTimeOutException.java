package com.aizuda.snail.job.client.common.exception;

import com.aizuda.snail.job.common.core.exception.BaseEasyRetryException;

/**
 * @author: opensnail
 * @date : 2022-03-03 14:49
 */
public class EasyRetryClientTimeOutException extends BaseEasyRetryException {

    public EasyRetryClientTimeOutException(String message) {
        super(message);
    }

    public EasyRetryClientTimeOutException(String message, Throwable cause) {
        super(message, cause);
    }

    public EasyRetryClientTimeOutException(Throwable cause) {
        super(cause);
    }

    public EasyRetryClientTimeOutException(String message, Object... arguments) {
        super(message, arguments);
    }

    public EasyRetryClientTimeOutException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public EasyRetryClientTimeOutException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public EasyRetryClientTimeOutException(String message, Object argument) {
        super(message, argument);
    }
}
