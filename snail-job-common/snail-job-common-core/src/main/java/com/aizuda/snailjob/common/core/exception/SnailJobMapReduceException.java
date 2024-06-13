
package com.aizuda.snailjob.common.core.exception;


/**
 * 异常信息
 *
 * @author: opensnail
 * @date : 2021-11-19 15:01
 */
public class SnailJobMapReduceException extends BaseSnailJobException {

    public SnailJobMapReduceException(String message) {
        super(message);
    }

    public SnailJobMapReduceException(String message, Object... arguments) {
        super(message, arguments);
    }

    public SnailJobMapReduceException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public SnailJobMapReduceException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public SnailJobMapReduceException(String message, Object argument) {
        super(message, argument);
    }
}
