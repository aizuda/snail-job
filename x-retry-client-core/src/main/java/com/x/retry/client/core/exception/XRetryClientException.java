package com.x.retry.client.core.exception;

import com.x.retry.common.core.exception.BaseXRetryException;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 14:49
 */
public class XRetryClientException extends BaseXRetryException {

    public XRetryClientException(String message) {
        super(message);
    }

    public XRetryClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public XRetryClientException(Throwable cause) {
        super(cause);
    }

    public XRetryClientException(String message, Object... arguments) {
        super(message, arguments);
    }

    public XRetryClientException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public XRetryClientException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public XRetryClientException(String message, Object argument) {
        super(message, argument);
    }
}
