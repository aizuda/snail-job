package com.aizuda.easy.retry.common.log.lang;

import java.util.Objects;

/**
 * 调用者。可以通过此类的方法获取调用者、多级调用者以及判断是否被调用
 *
 * @author wodeyangzipingpingwuqi
 */
public class LogCaller {
    private static final Caller INSTANCE;

    static {
        INSTANCE = tryCreateCaller();
    }

    /**
     * 获得调用者的调用者
     *
     * @return 调用者的调用者
     */
    public static Class<?> getCallerCaller() {
        return INSTANCE.getCallerCaller();
    }

    /**
     * 尝试创建{@link Caller}实现
     *
     * @return {@link Caller}实现
     */
    private static Caller tryCreateCaller() {
        Caller caller;
        try {
            caller = new StackWalkerCaller();
            if (Objects.nonNull(caller.getCallerCaller())) {
                return caller;
            }
        } catch (Throwable e) {
            //ignore
        }

        caller = new StackTraceCaller();
        return caller;
    }
}
