package com.aizuda.easy.retry.common.log.dialect.log4j;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.log.dialect.AbstractLog;
import com.aizuda.easy.retry.common.log.factory.LogFactory;
import com.aizuda.easy.retry.common.log.constant.LogFieldConstant;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

/**
 * <a href="http://logging.apache.org/log4j/1.2/index.html">Apache Log4J</a> log.<br>
 *
 * @author wodeyangzipingpingwuqi
 */
public class Log4jLog extends AbstractLog {
    private static final long serialVersionUID = -6843151523380063975L;

    private final Logger logger;

    // ------------------------------------------------------------------------- Constructor
    public Log4jLog(Logger logger) {
        this.logger = logger;
    }

    public Log4jLog(Class<?> clazz) {
        this((null == clazz) ? StrUtil.NULL : clazz.getName());
    }

    public Log4jLog(String name) {
        this(Logger.getLogger(name));
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
        log(fqcn, com.aizuda.easy.retry.common.log.level.Level.TRACE, t, format, remote, arguments);
    }

    // ------------------------------------------------------------------------- Debug
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
        log(fqcn, com.aizuda.easy.retry.common.log.level.Level.DEBUG, t, format, remote, arguments);
    }

    // ------------------------------------------------------------------------- Info
    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
        log(fqcn, com.aizuda.easy.retry.common.log.level.Level.INFO, t, format, remote, arguments);
    }

    // ------------------------------------------------------------------------- Warn
    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabledFor(org.apache.log4j.Level.WARN);
    }

    @Override
    public void warn(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
        log(fqcn, com.aizuda.easy.retry.common.log.level.Level.WARN, t, format, remote, arguments);
    }

    // ------------------------------------------------------------------------- Error
    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabledFor(org.apache.log4j.Level.ERROR);
    }

    @Override
    public void error(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
        log(fqcn, com.aizuda.easy.retry.common.log.level.Level.ERROR, t, format, remote, arguments);
    }

    // ------------------------------------------------------------------------- Log
    @Override
    public void log(String fqcn, com.aizuda.easy.retry.common.log.level.Level level, Throwable t, String format, Boolean remote, Object... arguments) {
        org.apache.log4j.Level log4jLevel;
        switch (level) {
            case TRACE:
                log4jLevel = org.apache.log4j.Level.TRACE;
                break;
            case DEBUG:
                log4jLevel = org.apache.log4j.Level.DEBUG;
                break;
            case INFO:
                log4jLevel = org.apache.log4j.Level.INFO;
                break;
            case WARN:
                log4jLevel = org.apache.log4j.Level.WARN;
                break;
            case ERROR:
                log4jLevel = Level.ERROR;
                break;
            default:
                throw new Error(StrUtil.format("Can not identify level: {}", level));
        }

        if (logger.isEnabledFor(log4jLevel)) {
            if (remote) {
                MDC.put(LogFieldConstant.MDC_REMOTE, remote.toString());
            }
            if (t == null) {
                t = LogFactory.extractThrowable(arguments);
            }
            logger.log(fqcn, log4jLevel, StrUtil.format(format, arguments), t);
        }
    }
}
