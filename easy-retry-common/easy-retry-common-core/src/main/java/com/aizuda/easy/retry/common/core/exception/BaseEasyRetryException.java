package com.aizuda.easy.retry.common.core.exception;

import org.slf4j.helpers.MessageFormatter;

/**
 * 异常信息
 *
 * @author: opensnail
 * @date : 2021-11-19 15:01
 */
public class BaseEasyRetryException extends RuntimeException {

    public BaseEasyRetryException(String message) {
        super(message);
    }

    public BaseEasyRetryException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseEasyRetryException(Throwable cause) {
        super(cause);
    }

    public BaseEasyRetryException(String message, Object ... arguments) {
        this(message, getArguments(arguments), getThrowable(arguments));
    }

    public BaseEasyRetryException(String message, Object[] arguments, Throwable cause) {
        super(MessageFormatter.arrayFormat(message, arguments).getMessage(), cause);
    }

    public BaseEasyRetryException(String message, Object argument, Throwable cause) {
        super(MessageFormatter.format(message, argument).getMessage(), cause);
    }

    public BaseEasyRetryException(String message, Object argument) {
        super(MessageFormatter.format(message, argument).getMessage());
    }

    public static Object[] getArguments(Object ... arguments) {

        Object argument = arguments[arguments.length - 1];
        if (argument instanceof Throwable) {

            Object[] dest = new Object[arguments.length - 1];
            System.arraycopy(arguments, 0, dest, 0,  arguments.length - 1);

            return dest;
        }

        return arguments;
    }

    public static Throwable getThrowable(Object ... arguments) {
        return arguments.length == getArguments(arguments).length ? null: (Throwable) arguments[arguments.length - 1];
    }
}
