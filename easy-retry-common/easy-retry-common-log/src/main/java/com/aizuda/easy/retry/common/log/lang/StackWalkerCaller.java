package com.aizuda.easy.retry.common.log.lang;

import cn.hutool.core.exceptions.UtilException;

import java.io.Serializable;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author xiaowoniu
 * @date 2024-01-31 23:32:15
 * @since 2.6.0
 */
public class StackWalkerCaller implements Caller, Serializable {

    @Override
    public Class<?> getCaller() {
        StackWalker instance = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        StackWalker.StackFrame walk = (StackWalker.StackFrame) instance
                .walk((Function<Stream<StackWalker.StackFrame>, Object>) stackFrameStream ->
                        stackFrameStream.skip(2).findFirst().get());
        try {
            return Class.forName(walk.getClassName());
        } catch (ClassNotFoundException e) {
            throw new UtilException(e, "[{}] not found!", walk.getClassName());
        }
    }

    @Override
    public Class<?> getCallerCaller() {

        StackWalker instance = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        StackWalker.StackFrame walk = (StackWalker.StackFrame) instance
                .walk((Function<Stream<StackWalker.StackFrame>, Object>) stackFrameStream ->
                        stackFrameStream.skip(3).findFirst().get());
        try {
            return Class.forName(walk.getClassName());
        } catch (ClassNotFoundException e) {
            throw new UtilException(e, "[{}] not found!", walk.getClassName());
        }
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
