package com.aizuda.snail.job.server.common.exception;


import com.aizuda.snail.job.common.core.exception.BaseEasyRetryException;

/**
 * 服务端异常类
 *
 * @author: opensnail
 * @date : 2021-11-19 15:01
 */
public class EasyRetryServerException extends BaseEasyRetryException {

    public EasyRetryServerException(String message) {
        super(message);
    }

    public EasyRetryServerException(String message, Object ... arguments) {
        super(message, arguments);
    }
}
