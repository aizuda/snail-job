package com.aizuda.easy.retry.common.log;

import com.aizuda.easy.retry.common.core.util.EnvironmentUtils;
import com.aizuda.easy.retry.common.log.lang.LogCaller;
import com.aizuda.easy.retry.common.log.level.Level;

/**
 * @author zhengweilin
 * @version 2.6.0
 * @date 2024/01/09
 */
public final class Local {


    // 完全限定类名
    private static final String FQCN = Local.class.getName();

    // ----------------------------------------------------------- Log method start
    // ------------------------ Trace

    /**
     * Trace等级日志，小于debug<br>
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void trace(String format, Object... arguments) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        trace(LogFactory.get(LogCaller.getCallerCaller()), format, arguments);
    }

    /**
     * Trace等级日志，小于Debug
     *
     * @param log       日志对象
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void trace(Log log, String format, Object... arguments) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        log.trace(FQCN, null, format, false, arguments);
    }

    // ------------------------ debug

    /**
     * Debug等级日志，小于Info<br>
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void debug(String format, Object... arguments) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        debug(LogFactory.get(LogCaller.getCallerCaller()), format, arguments);
    }

    /**
     * Debug等级日志，小于Info
     *
     * @param log       日志对象
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void debug(Log log, String format, Object... arguments) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        log.debug(FQCN, null, format, false, arguments);
    }

    // ------------------------ info

    /**
     * Info等级日志，小于Warn<br>
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void info(String format, Object... arguments) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        info(LogFactory.get(LogCaller.getCallerCaller()), format, arguments);
    }

    /**
     * Info等级日志，小于Warn
     *
     * @param log       日志对象
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void info(Log log, String format, Object... arguments) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        log.info(FQCN, null, format, false, arguments);
    }

    // ------------------------ warn

    /**
     * Warn等级日志，小于Error<br>
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void warn(String format, Object... arguments) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        warn(LogFactory.get(LogCaller.getCallerCaller()), format, arguments);
    }

    /**
     * Warn等级日志，小于Error<br>
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param e         需在日志中堆栈打印的异常
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void warn(Throwable e, String format, Object... arguments) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        warn(LogFactory.get(LogCaller.getCallerCaller()), e, format, arguments);
    }

    /**
     * Warn等级日志，小于Error
     *
     * @param log       日志对象
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void warn(Log log, String format, Object... arguments) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        warn(log, null, format, arguments);
    }

    /**
     * Warn等级日志，小于Error
     *
     * @param log       日志对象
     * @param e         需在日志中堆栈打印的异常
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void warn(Log log, Throwable e, String format, Object... arguments) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        log.warn(FQCN, e, format, false, arguments);
    }

    // ------------------------ error

    /**
     * Error等级日志<br>
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param e 需在日志中堆栈打印的异常
     */
    public void error(Throwable e) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        error(LogFactory.get(LogCaller.getCallerCaller()), e);
    }

    /**
     * Error等级日志<br>
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void error(String format, Object... arguments) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        error(LogFactory.get(LogCaller.getCallerCaller()), format, arguments);
    }

    /**
     * Error等级日志<br>
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param e         需在日志中堆栈打印的异常
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void error(Throwable e, String format, Object... arguments) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        error(LogFactory.get(LogCaller.getCallerCaller()), e, format, arguments);
    }

    /**
     * Error等级日志<br>
     *
     * @param log 日志对象
     * @param e   需在日志中堆栈打印的异常
     */
    public void error(Log log, Throwable e) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        error(log, e, e.getMessage());
    }

    /**
     * Error等级日志<br>
     *
     * @param log       日志对象
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void error(Log log, String format, Object... arguments) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        error(log, null, format, arguments);
    }

    /**
     * Error等级日志<br>
     *
     * @param log       日志对象
     * @param e         需在日志中堆栈打印的异常
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void error(Log log, Throwable e, String format, Object... arguments) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        log.error(FQCN, e, format, false, arguments);
    }

    // ------------------------ Log

    /**
     * 打印日志<br>
     *
     * @param level     日志级别
     * @param t         需在日志中堆栈打印的异常
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void log(Level level, Throwable t, String format, Boolean remote, Object... arguments) {
        if (!EnvironmentUtils.getLogStatus()) {
            return;
        }

        LogFactory.get(LogCaller.getCallerCaller()).log(FQCN, level, t, format, remote, arguments);
    }
}
