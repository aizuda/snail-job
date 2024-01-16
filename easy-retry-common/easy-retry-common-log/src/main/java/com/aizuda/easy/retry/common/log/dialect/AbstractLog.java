package com.aizuda.easy.retry.common.log.dialect;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.log.level.Level;

import java.io.Serializable;

/**
 * 抽象日志类<br>
 * 实现了一些通用的接口
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
    public void trace(Throwable t, Boolean remote) {
        trace(t, ExceptionUtil.getSimpleMessage(t), remote);
    }

    @Override
    public void trace(String format, Boolean remote, Object... arguments) {
        trace(null, format, remote, arguments);
    }

    @Override
    public void trace(Throwable t, String format, Boolean remote, Object... arguments) {
        trace(FQCN, t, format, remote, arguments);
    }

    @Override
    public void debug(Throwable t, Boolean remote) {
        debug(t, ExceptionUtil.getSimpleMessage(t), remote);
    }

    @Override
    public void debug(String format, Boolean remote, Object... arguments) {
        if (null != arguments && 1 == arguments.length && arguments[0] instanceof Throwable) {
            // 兼容Slf4j中的xxx(String message, Throwable e)
            debug((Throwable) arguments[0], format, remote);
        } else {
            debug(null, format, remote, arguments);
        }
    }

    @Override
    public void debug(Throwable t, String format, Boolean remote, Object... arguments) {
        debug(FQCN, t, format, remote, arguments);
    }

    @Override
    public void info(Throwable t, Boolean remote) {
        info(t, ExceptionUtil.getSimpleMessage(t), remote);
    }

    @Override
    public void info(String format, Boolean remote, Object... arguments) {
        if (null != arguments && 1 == arguments.length && arguments[0] instanceof Throwable) {
            // 兼容Slf4j中的xxx(String message, Throwable e)
            info((Throwable) arguments[0], format, remote);
        } else {
            info(null, format, remote, arguments);
        }
    }

    @Override
    public void info(Throwable t, String format, Boolean remote, Object... arguments) {
        info(FQCN, t, format, remote, arguments);
    }

    @Override
    public void warn(Throwable t, Boolean remote) {
        warn(t, ExceptionUtil.getSimpleMessage(t), remote);
    }

    @Override
    public void warn(String format, Boolean remote, Object... arguments) {
        if (null != arguments && 1 == arguments.length && arguments[0] instanceof Throwable) {
            // 兼容Slf4j中的xxx(String message, Throwable e)
            warn((Throwable) arguments[0], format, remote);
        } else {
            warn(null, format, remote, arguments);
        }
    }

    @Override
    public void warn(Throwable t, String format, Boolean remote, Object... arguments) {
        warn(FQCN, t, format, remote, arguments);
    }

    @Override
    public void error(Throwable t, Boolean remote) {
        this.error(t, ExceptionUtil.getSimpleMessage(t), remote);
    }

    @Override
    public void error(String format, Boolean remote, Object... arguments) {
        if (null != arguments && 1 == arguments.length && arguments[0] instanceof Throwable) {
            // 兼容Slf4j中的xxx(String message, Throwable e)
            error((Throwable) arguments[0], format, remote);
        } else {
            error(null, format, remote, arguments);
        }
    }

    @Override
    public void error(Throwable t, String format, Boolean remote, Object... arguments) {
        error(FQCN, t, format, remote, arguments);
    }

    @Override
    public void log(Level level, String format, Boolean remote, Object... arguments) {
        if (null != arguments && 1 == arguments.length && arguments[0] instanceof Throwable) {
            // 兼容Slf4j中的xxx(String message, Throwable e)
            log(level, (Throwable) arguments[0], format, remote);
        } else {
            log(level, null, format, remote, arguments);
        }
    }

    @Override
    public void log(Level level, Throwable t, String format, Boolean remote, Object... arguments) {
        this.log(FQCN, level, t, format, remote, arguments);
    }
}
