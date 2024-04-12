package com.aizuda.easy.retry.client.common.exception;

import com.aizuda.easy.retry.common.core.exception.BaseEasyRetryException;

/**
 * @author: opensnail
 * @date : 2022-03-03 14:49
 */
public class EasyRetryClientException extends BaseEasyRetryException {

    public EasyRetryClientException(String message) {
        super(message);
    }

    public EasyRetryClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public EasyRetryClientException(Throwable cause) {
        super(cause);
    }

    public EasyRetryClientException(String message, Object... arguments) {
        super(message, arguments);
    }

    public EasyRetryClientException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public EasyRetryClientException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public EasyRetryClientException(String message, Object argument) {
        super(message, argument);
    }
}
