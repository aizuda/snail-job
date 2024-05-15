package com.aizuda.snailjob.common.log.dialect.log4j;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.log.dialect.AbstractLog;
import com.aizuda.snailjob.common.log.factory.LogFactory;
import com.aizuda.snailjob.common.log.constant.LogFieldConstants;
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
    public void trace(Boolean remote, String fqcn, String format, Object... arguments) {
        log(com.aizuda.snailjob.common.log.level.Level.TRACE, remote, fqcn, format, arguments);
    }

    // ------------------------------------------------------------------------- Debug
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(Boolean remote, String fqcn, String format, Object... arguments) {
        log(com.aizuda.snailjob.common.log.level.Level.DEBUG, remote, fqcn, format, arguments);
    }

    // ------------------------------------------------------------------------- Info
    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(Boolean remote, String fqcn, String format, Object... arguments) {
        log(com.aizuda.snailjob.common.log.level.Level.INFO, remote, fqcn, format, arguments);
    }

    // ------------------------------------------------------------------------- Warn
    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabledFor(org.apache.log4j.Level.WARN);
    }

    @Override
    public void warn(Boolean remote, String fqcn, String format, Object... arguments) {
        log(com.aizuda.snailjob.common.log.level.Level.WARN, remote, fqcn, format, arguments);
    }

    // ------------------------------------------------------------------------- Error
    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabledFor(org.apache.log4j.Level.ERROR);
    }

    @Override
    public void error(Boolean remote, String fqcn, String format, Object... arguments) {
        log(com.aizuda.snailjob.common.log.level.Level.ERROR, remote, fqcn, format, arguments);
    }

    // ------------------------------------------------------------------------- Log
    @Override
    public void log(com.aizuda.snailjob.common.log.level.Level level, Boolean remote, String fqcn, String format,
                    Object... arguments) {
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
                MDC.put(LogFieldConstants.MDC_REMOTE, remote.toString());
            }

            logger.log(fqcn, log4jLevel, StrUtil.format(format, arguments), LogFactory.extractThrowable(arguments));
        }
    }
}
