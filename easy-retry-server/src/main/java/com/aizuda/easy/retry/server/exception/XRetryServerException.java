package com.aizuda.easy.retry.server.exception;

import com.aizuda.easy.retry.common.core.exception.BaseXRetryException;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-19 15:01
 */
public class XRetryServerException extends BaseXRetryException {

    public XRetryServerException(String message) {
        super(message);
    }

    public XRetryServerException(String message, Object ... arguments) {
        super(message, arguments);
    }
}
