package com.aizuda.easy.retry.common.log.dialect.slf4j;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.constant.LogFieldConstant;
import com.aizuda.easy.retry.common.log.AbstractLog;
import com.aizuda.easy.retry.common.log.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.spi.LocationAwareLogger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <a href="http://www.slf4j.org/">SLF4J</a> log.<br>
 * 同样无缝支持 <a href="http://logback.qos.ch/">LogBack</a>
 *
 * @author wodeyangzipingpingwuqi
 */
public class Slf4jLog extends AbstractLog {
    private static final long serialVersionUID = -6843151523380063975L;

    private final transient Logger logger;
    /**
     * 是否为 LocationAwareLogger ，用于判断是否可以传递FQCN
     */
    private final boolean isLocationAwareLogger;

    // ------------------------------------------------------------------------- Constructor
    public Slf4jLog(Logger logger) {
        this.logger = logger;
        this.isLocationAwareLogger = (logger instanceof LocationAwareLogger);
    }

    public Slf4jLog(Class<?> clazz) {
        this(getSlf4jLogger(clazz));
    }

    public Slf4jLog(String name) {
        this(LoggerFactory.getLogger(name));
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
        if (isTraceEnabled()) {
            setContextMap(remote);
            if (this.isLocationAwareLogger) {
                locationAwareLog((LocationAwareLogger) this.logger, fqcn, LocationAwareLogger.TRACE_INT, t, format, arguments);
            } else {
                if (Objects.nonNull(t)) {
                    this.logger.trace(StrUtil.format(format, arguments), t);
                } else {
                    this.logger.trace(format, arguments);
                }
            }
        }
    }

    // ------------------------------------------------------------------------- Debug
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
        if (isDebugEnabled()) {
            setContextMap(remote);
            if (this.isLocationAwareLogger) {
                locationAwareLog((LocationAwareLogger) this.logger, fqcn, LocationAwareLogger.DEBUG_INT, t, format, arguments);
            } else {
                if (Objects.nonNull(t)) {
                    this.logger.debug(StrUtil.format(format, arguments), t);
                } else {
                    this.logger.debug(format, arguments);
                }
            }
        }
    }

    // ------------------------------------------------------------------------- Info
    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
        if (isInfoEnabled()) {
            setContextMap(remote);
            if (this.isLocationAwareLogger) {
                locationAwareLog((LocationAwareLogger) this.logger, fqcn, LocationAwareLogger.INFO_INT, t, format, arguments);
            } else {
                if (Objects.nonNull(t)) {
                    this.logger.info(StrUtil.format(format, arguments), t);
                } else {
                    this.logger.info(format, arguments);
                }
            }
        }
    }

    // ------------------------------------------------------------------------- Warn
    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
        if (isWarnEnabled()) {
            setContextMap(remote);
            if (this.isLocationAwareLogger) {
                locationAwareLog((LocationAwareLogger) this.logger, fqcn, LocationAwareLogger.WARN_INT, t, format, arguments);
            } else {
                if (Objects.nonNull(t)) {
                    this.logger.warn(StrUtil.format(format, arguments), t);
                } else {
                    this.logger.warn(format, arguments);
                }
            }
        }
    }

    // ------------------------------------------------------------------------- Error
    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
        if (isErrorEnabled()) {
            setContextMap(remote);
            if (this.isLocationAwareLogger) {
                locationAwareLog((LocationAwareLogger) this.logger, fqcn, LocationAwareLogger.ERROR_INT, t, format, arguments);
            } else {
                if (Objects.nonNull(t)) {
                    this.logger.error(StrUtil.format(format, arguments), t);
                } else {
                    this.logger.error(format, arguments);
                }
            }
        }
    }

    // ------------------------------------------------------------------------- Log
    @Override
    public void log(String fqcn, Level level, Throwable t, String format, Boolean remote, Object... arguments) {
        switch (level) {
            case TRACE:
                trace(fqcn, t, format, remote, arguments);
                break;
            case DEBUG:
                debug(fqcn, t, format, remote, arguments);
                break;
            case INFO:
                info(fqcn, t, format, remote, arguments);
                break;
            case WARN:
                warn(fqcn, t, format, remote, arguments);
                break;
            case ERROR:
                error(fqcn, t, format, remote, arguments);
                break;
            case OFF:
                break;
            default:
                throw new Error(StrUtil.format("Can not identify level: {}", level));
        }
    }

    // -------------------------------------------------------------------------------------------------- Private method

    /**
     * 打印日志<br>
     * 此方法用于兼容底层日志实现，通过传入当前包装类名，以解决打印日志中行号错误问题
     *
     * @param logger      {@link LocationAwareLogger} 实现
     * @param fqcn        完全限定类名(Fully Qualified Class Name)，用于纠正定位错误行号
     * @param level_int   日志级别，使用LocationAwareLogger中的常量
     * @param t           异常
     * @param msgTemplate 消息模板
     * @param arguments   参数
     */
    private void locationAwareLog(LocationAwareLogger logger, String fqcn, int level_int, Throwable t, String msgTemplate, Object[] arguments) {
        // ((LocationAwareLogger)this.logger).log(null, fqcn, level_int, msgTemplate, arguments, t);
        // 由于slf4j-log4j12中此方法的实现存在bug，故在此拼接参数
        logger.log(null, fqcn, level_int, msgTemplate, arguments, t);
    }

    /**
     * 获取Slf4j Logger对象
     *
     * @param clazz 打印日志所在类，当为{@code null}时使用“null”表示
     * @return {@link Logger}
     */
    private static Logger getSlf4jLogger(Class<?> clazz) {
        return (null == clazz) ? LoggerFactory.getLogger(StrUtil.EMPTY) : LoggerFactory.getLogger(clazz);
    }

    /**
     * 设置MDC上下文
     *
     * @param remote
     */
    private void setContextMap(Boolean remote) {
        if (remote) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put(LogFieldConstant.MDC_REMOTE, remote.toString());
            MDC.setContextMap(map);
        }
    }
}
