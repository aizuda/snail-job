package com.aizuda.easy.retry.server.job.task.support.appender;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.aizuda.easy.retry.common.core.constant.LogFieldConstant;
import com.aizuda.easy.retry.common.core.log.LogContentDTO;
import org.slf4j.MDC;

import java.util.Objects;

/**
 * @author wodeyangzipingpingwuqi
 * @date 2023-12-27
 * @since 2.6.0
 */
public class EasyRetryServerLogbackAppender<E> extends UnsynchronizedAppenderBase<E> {

    @Override
    public void start() {
        super.start();
    }

    @Override
    protected void append(E eventObject) {

        // Not job context
        if (!(eventObject instanceof LoggingEvent) || Objects.isNull(MDC.getMDCAdapter().get(LogFieldConstant.MDC_REMOTE))) {
            return;
        }

        MDC.getMDCAdapter().remove(LogFieldConstant.MDC_REMOTE);

        // Prepare processing
        ((LoggingEvent) eventObject).prepareForDeferredProcessing();
        LoggingEvent event = (LoggingEvent) eventObject;

        LogContentDTO logContentDTO = new LogContentDTO();
        logContentDTO.addTimeStamp(event.getTimeStamp());
        logContentDTO.addLevelField(event.getLevel().levelStr);
        logContentDTO.addThreadField(event.getThreadName());
        logContentDTO.addMessageField(event.getFormattedMessage());
        logContentDTO.addLocationField(getLocationField(event));
        logContentDTO.addThrowableField(getThrowableField(event));


    }

    private String getThrowableField(LoggingEvent event) {
        IThrowableProxy iThrowableProxy = event.getThrowableProxy();
        if (iThrowableProxy != null) {
            String throwable = getExceptionInfo(iThrowableProxy);
            throwable += formatThrowable(event.getThrowableProxy().getStackTraceElementProxyArray());
            return throwable;
        }
        return null;
    }

    private String getLocationField(LoggingEvent event) {
        StackTraceElement[] caller = event.getCallerData();
        if (caller != null && caller.length > 0) {
            return caller[0].toString();
        }
        return null;
    }

    private String formatThrowable(StackTraceElementProxy[] stackTraceElementProxyArray) {
        StringBuilder builder = new StringBuilder();
        for (StackTraceElementProxy step : stackTraceElementProxyArray) {
            builder.append(CoreConstants.LINE_SEPARATOR);
            String string = step.toString();
            builder.append(CoreConstants.TAB).append(string);
            ThrowableProxyUtil.subjoinPackagingData(builder, step);
        }
        return builder.toString();
    }

    private String getExceptionInfo(IThrowableProxy iThrowableProxy) {
        String s = iThrowableProxy.getClassName();
        String message = iThrowableProxy.getMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
