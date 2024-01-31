package com.aizuda.easy.retry.common.log.lang;

import java.io.Serializable;

/**
 * @author xiaowoniu
 * @date 2024-01-31 23:32:15
 * @since 2.6.0
 */
public class StackWalkerCaller implements Caller, Serializable {

    @Override
    public Class<?> getCaller() {
        return null;
    }

    @Override
    public Class<?> getCallerCaller() {

        StackWalker instance = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        Class<?> callerClass = instance.getCallerClass();
        return callerClass;
    }

    @Override
    public Class<?> getCaller(int depth) {
        return null;
    }

    @Override
    public boolean isCalledBy(Class<?> clazz) {
        return false;
    }
}
