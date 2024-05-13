package com.aizuda.snailjob.client.common.exception;

import com.aizuda.snailjob.common.core.exception.BaseSnailJobException;

/**
 * @author: opensnail
 * @date : 2022-03-03 14:49
 */
public class SnailJobRemoteException extends BaseSnailJobException {

    public SnailJobRemoteException(String message) {
        super(message);
    }

    public SnailJobRemoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public SnailJobRemoteException(Throwable cause) {
        super(cause);
    }

    public SnailJobRemoteException(String message, Object... arguments) {
        super(message, arguments);
    }

    public SnailJobRemoteException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public SnailJobRemoteException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public SnailJobRemoteException(String message, Object argument) {
        super(message, argument);
    }
}
