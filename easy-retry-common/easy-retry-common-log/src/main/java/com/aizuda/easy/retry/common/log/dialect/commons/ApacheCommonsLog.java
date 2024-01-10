package com.aizuda.easy.retry.common.log.dialect.commons;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.log.AbstractLog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Apache Commons Logging
 * @author wodeyangzipingpingwuqi
 *
 */
public class ApacheCommonsLog extends AbstractLog {
	private static final long serialVersionUID = -6843151523380063975L;
	
	private final transient Log logger;
	private final String name;

	// ------------------------------------------------------------------------- Constructor
	public ApacheCommonsLog(Log logger, String name) {
		this.logger = logger;
		this.name = name;
	}

	public ApacheCommonsLog(Class<?> clazz) {
		this(LogFactory.getLog(clazz), null == clazz ? StrUtil.NULL : clazz.getName());
	}

	public ApacheCommonsLog(String name) {
		this(LogFactory.getLog(name), name);
	}

	@Override
	public String getName() {
		return this.name;
	}

	// ------------------------------------------------------------------------- Trace
	@Override
	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}

	@Override
	public void trace(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
		// fqcn此处无效
		if(isTraceEnabled()){
			logger.trace(StrUtil.format(format, arguments), t);
		}
	}

	// ------------------------------------------------------------------------- Debug
	@Override
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	@Override
	public void debug(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
		// fqcn此处无效
		if(isDebugEnabled()){
			logger.debug(StrUtil.format(format, arguments), t);
		}
	}

	// ------------------------------------------------------------------------- Info
	@Override
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	@Override
	public void info(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
		// fqcn此处无效
		if(isInfoEnabled()){
			logger.info(StrUtil.format(format, arguments), t);
		}
	}

	// ------------------------------------------------------------------------- Warn
	@Override
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}

	@Override
	public void warn(String format, Boolean remote, Object... arguments) {
		if(isWarnEnabled()){
			logger.warn(StrUtil.format(format, arguments));
		}
	}

	@Override
	public void warn(Throwable t, String format, Boolean remote, Object... arguments) {
	}
	
	@Override
	public void warn(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
		// fqcn此处无效
		if(isWarnEnabled()){
			logger.warn(StrUtil.format(format, arguments), t);
		}
	}

	// ------------------------------------------------------------------------- Error
	@Override
	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
	}

	@Override
	public void error(String fqcn, Throwable t, String format, Boolean remote, Object... arguments) {
		// fqcn此处无效
		if(isErrorEnabled()){
			logger.warn(StrUtil.format(format, arguments), t);
		}
		
	}
	
	// ------------------------------------------------------------------------- Log
	@Override
	public void log(String fqcn, com.aizuda.easy.retry.common.log.level.Level level, Throwable t, String format, Boolean remote, Object... arguments) {
		switch (level) {
		case TRACE:
			trace(t, format, remote, arguments);
			break;
		case DEBUG:
			debug(t, format, remote, arguments);
			break;
		case INFO:
			info(t, format, remote, arguments);
			break;
		case WARN:
			warn(t, format, remote, arguments);
			break;
		case ERROR:
			error(t, format, remote, arguments);
			break;
		default:
			throw new Error(StrUtil.format("Can not identify level: {}", level));
		}
	}
	// ------------------------------------------------------------------------- Private method
}
