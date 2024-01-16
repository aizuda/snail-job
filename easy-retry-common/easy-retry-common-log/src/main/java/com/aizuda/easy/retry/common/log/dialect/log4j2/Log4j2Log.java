package com.aizuda.easy.retry.common.log.dialect.log4j2;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.log.dialect.AbstractLog;
import com.aizuda.easy.retry.common.log.factory.LogFactory;
import com.aizuda.easy.retry.common.log.constant.LogFieldConstant;
import org.apache.log4j.MDC;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.AbstractLogger;

/**
 * <a href="http://logging.apache.org/log4j/2.x/index.html">Apache Log4J 2</a> log.<br>
 *
 * @author wodeyangzipingpingwuqi
 */
public class Log4j2Log extends AbstractLog {
    private static final long serialVersionUID = -6843151523380063975L;

    private final transient Logger logger;

    // ------------------------------------------------------------------------- Constructor
    public Log4j2Log(Logger logger) {
        this.logger = logger;
    }

    public Log4j2Log(Class<?> clazz) {
        this(LogManager.getLogger(clazz));
    }

    public Log4j2Log(String name) {
        this(LogManager.getLogger(name));
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    // ------------------------------------------------------------------------- Trace
    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
        logIfEnabled(fqcn, Level.TRACE, t, format, remote, arguments);
    }

    // ------------------------------------------------------------------------- Debug
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
        logIfEnabled(fqcn, Level.DEBUG, t, format, remote, arguments);
    }

    // ------------------------------------------------------------------------- Info
    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
        logIfEnabled(fqcn, Level.INFO, t, format, remote, arguments);
    }

    // ------------------------------------------------------------------------- Warn
    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
        logIfEnabled(fqcn, Level.WARN, t, format, remote, arguments);
    }

    // ------------------------------------------------------------------------- Error
    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
        logIfEnabled(fqcn, Level.ERROR, t, format, remote, arguments);
    }

    // ------------------------------------------------------------------------- Log
    @Override
    public void log(String fqcn, com.aizuda.easy.retry.common.log.level.Level level, Throwable t, String format, Boolean remote, Object... arguments) {
        Level log4j2Level;
        switch (level) {
            case TRACE:
                log4j2Level = Level.TRACE;
                break;
            case DEBUG:
                log4j2Level = Level.DEBUG;
                break;
            case INFO:
                log4j2Level = Level.INFO;
                break;
            case WARN:
                log4j2Level = Level.WARN;
                break;
            case ERROR:
                log4j2Level = Level.ERROR;
                break;
            default:
                throw new Error(StrUtil.format("Can not identify level: {}", level));
        }
        logIfEnabled(fqcn, log4j2Level, t, format, remote, arguments);
    }

    // ------------------------------------------------------------------------- Private method

    /**
     * 打印日志<br>
     * 此方法用于兼容底层日志实现，通过传入当前包装类名，以解决打印日志中行号错误问题
     *
     * @param fqcn        完全限定类名(Fully Qualified Class Name)，用于纠正定位错误行号
     * @param level       日志级别，使用org.apache.logging.log4j.Level中的常量
     * @param t           异常
     * @param msgTemplate 消息模板
     * @param arguments   参数
     */
    private void logIfEnabled(String fqcn, Level level, Throwable t, String msgTemplate, Boolean remote, Object... arguments) {
        if (this.logger.isEnabled(level)) {

            if (remote) {
                MDC.put(LogFieldConstant.MDC_REMOTE, remote.toString());
            }
            if (this.logger instanceof AbstractLogger) {
                ((AbstractLogger) this.logger).logIfEnabled(fqcn, level, null, StrUtil.format(msgTemplate, arguments), t);
            } else {
                // FQCN无效
                //this.logger.log(level, StrUtil.format(msgTemplate, arguments), t);
                if (t == null) {
                    t = LogFactory.extractThrowable(arguments);
                }
                this.logger.log(level, StrUtil.format(msgTemplate, arguments), t);
            }
        }
    }
}
