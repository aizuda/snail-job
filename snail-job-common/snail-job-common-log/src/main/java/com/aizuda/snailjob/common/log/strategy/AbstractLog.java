package com.aizuda.snailjob.common.log.strategy;

import com.aizuda.snailjob.common.log.dialect.Log;
import com.aizuda.snailjob.common.log.factory.GlobalLogFactory;
import com.aizuda.snailjob.common.log.factory.LogFactory;
import com.aizuda.snailjob.common.log.lang.LogCaller;
import com.aizuda.snailjob.common.log.level.Level;

/**
 * @author: opensnail
 * @date : 2024-05-03
 */
public abstract class AbstractLog {
    // 完全限定类名
    private static final String FQCN = Local.class.getName();
    private Boolean isRemote = Boolean.FALSE;

    public Boolean getRemote() {
        return isRemote;
    }

    protected void setRemote(final Boolean remote) {
        isRemote = remote;
    }

    /**
     * Trace等级日志，小于debug<br> 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void trace(String format, Object... arguments) {
        if (!GlobalLogFactory.logSwitch()) {
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
        if (!GlobalLogFactory.logSwitch()) {
            return;
        }

        log.trace(isRemote, FQCN, format, arguments);
    }

    // ------------------------ debug

    /**
     * Debug等级日志，小于Info<br> 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void debug(String format, Object... arguments) {
        if (!GlobalLogFactory.logSwitch()) {
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
        if (!GlobalLogFactory.logSwitch()) {
            return;
        }

        log.debug(isRemote, FQCN, format, arguments);
    }

    // ------------------------ info

    /**
     * Info等级日志，小于Warn<br> 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void info(String format, Object... arguments) {
        if (!GlobalLogFactory.logSwitch()) {
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
        if (!GlobalLogFactory.logSwitch()) {
            return;
        }

        log.info(isRemote, FQCN, format, arguments);
    }

    // ------------------------ warn

    /**
     * Warn等级日志，小于Error<br> 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void warn(String format, Object... arguments) {
        if (!GlobalLogFactory.logSwitch()) {
            return;
        }

        warn(LogFactory.get(LogCaller.getCallerCaller()), format, arguments);
    }

    /**
     * Warn等级日志，小于Error
     *
     * @param log       日志对象
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void warn(Log log, String format, Object... arguments) {
        if (!GlobalLogFactory.logSwitch()) {
            return;
        }

        log.warn(isRemote, FQCN, format, arguments);
    }

    // ------------------------ error

    /**
     * Error等级日志<br> 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param e 需在日志中堆栈打印的异常
     */
    public void error(Throwable e) {
        if (!GlobalLogFactory.logSwitch()) {
            return;
        }

        error(LogFactory.get(LogCaller.getCallerCaller()), e);
    }

    /**
     * Error等级日志<br> 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void error(String format, Object... arguments) {
        if (!GlobalLogFactory.logSwitch()) {
            return;
        }

        error(LogFactory.get(LogCaller.getCallerCaller()), format, arguments);
    }

    /**
     * Error等级日志<br>
     *
     * @param log 日志对象
     * @param e   需在日志中堆栈打印的异常
     */
    public void error(Log log, Throwable e) {
        if (!GlobalLogFactory.logSwitch()) {
            return;
        }

        log.error(false, e);
    }

    /**
     * Error等级日志<br>
     *
     * @param log       日志对象
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void error(Log log, String format, Object... arguments) {
        if (!GlobalLogFactory.logSwitch()) {
            return;
        }

        log.error(isRemote, FQCN, format, arguments);
    }

    // ------------------------ Log

    /**
     * 打印日志<br>
     *
     * @param level     日志级别
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public void log(Level level, Boolean remote, String format, Object... arguments) {
        if (!GlobalLogFactory.logSwitch()) {
            return;
        }

        LogFactory.get(LogCaller.getCallerCaller()).log(level, remote, FQCN, format, arguments);
    }
}
