package com.aizuda.snailjob.common.log.dialect;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.log.level.Level;

import java.io.Serializable;

/**
 * 抽象日志类<br> 实现了一些通用的接口
 *
 * @author wodeyangzipingpingwuqi
 */
public abstract class AbstractLog implements Log, Serializable {

    private static final long serialVersionUID = -3211115409504005616L;
    private static final String FQCN = AbstractLog.class.getName();

    @Override
    public boolean isEnabled(Level level) {
        switch (level) {
            case TRACE:
                return isTraceEnabled();
            case DEBUG:
                return isDebugEnabled();
            case INFO:
                return isInfoEnabled();
            case WARN:
                return isWarnEnabled();
            case ERROR:
                return isErrorEnabled();
            default:
                throw new Error(StrUtil.format("Can not identify level: {}", level));
        }
    }

    @Override
    public void trace(final Boolean remote, final Throwable t) {
        trace(remote, "", t);
    }

    @Override
    public void trace(final Boolean remote, final String msg) {
        trace(remote, FQCN, "", msg);
    }

    @Override
    public void trace(final Boolean remote, final String msg, final Throwable t) {
        trace(remote, FQCN, msg, t);
    }

    @Override
    public void trace(Boolean remote, String format, Object... arguments) {
        trace(remote, FQCN, format, arguments);
    }

    @Override
    public void debug(final Boolean remote, final Throwable t) {
        debug(remote, "", t);
    }

    @Override
    public void debug(final Boolean remote, final String msg, final Throwable t) {
        debug(remote, FQCN, msg, t);
    }

    @Override
    public void debug(final Boolean remote, final String msg) {
        debug(remote, FQCN, "", msg);
    }

    @Override
    public void debug(Boolean remote, String format, Object... arguments) {
        debug(remote, FQCN, format, arguments);
    }

    @Override
    public void info(Boolean remote, Throwable t) {
        info(remote, FQCN, "", t);
    }


    @Override
    public void info(final Boolean remote, final String msg, final Throwable t) {
        info(remote, FQCN, msg, t);
    }

    @Override
    public void info(final Boolean remote, final String msg) {
        info(remote, FQCN, msg);
    }

    @Override
    public void info(Boolean remote, String format, Object... arguments) {
        info(remote, FQCN, format, arguments);
    }

    @Override
    public void warn(Boolean remote, String format, Object... arguments) {
        warn(remote, FQCN, format, arguments);
    }

    @Override
    public void error(final Boolean remote, final String msg, final Throwable t) {
        error(remote, FQCN, msg, t);
    }

    @Override
    public void error(final Boolean remote, final String msg) {
        error(remote, FQCN, msg);
    }

    @Override
    public void error(final Boolean remote, final Throwable t) {
        error(remote, FQCN, "", t);
    }

    @Override
    public void error(Boolean remote, String format, Object... arguments) {
        error(remote, FQCN, format, arguments);
    }

    @Override
    public void warn(final Boolean remote, final String msg, final Throwable t) {
        warn(remote, FQCN, msg, t);
    }

    @Override
    public void warn(final Boolean remote, final String msg) {
        warn(remote, FQCN, msg);
    }

    @Override
    public void warn(final Boolean remote, final Throwable t) {
        warn(remote, FQCN, "", t);
    }

    @Override
    public void log(Level level, Boolean remote, String format, Object... arguments) {
        this.log(level, remote, FQCN, format, arguments);
    }
}
