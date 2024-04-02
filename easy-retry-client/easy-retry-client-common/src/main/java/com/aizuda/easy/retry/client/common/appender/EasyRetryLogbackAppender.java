package com.aizuda.easy.retry.client.common.appender;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.aizuda.easy.retry.client.common.log.report.LogReportFactory;
import com.aizuda.easy.retry.client.common.log.support.EasyRetryLogManager;
import com.aizuda.easy.retry.client.common.netty.NettyChannel;
import com.aizuda.easy.retry.common.log.dto.LogContentDTO;
import com.aizuda.easy.retry.common.log.constant.LogFieldConstants;
import org.slf4j.MDC;

import java.util.Objects;
import java.util.Optional;

/**
 * @author wodeyangzipingpingwuqi
 * @date 2023-12-27
 * @since 2.6.0
 */
public class EasyRetryLogbackAppender<E> extends UnsynchronizedAppenderBase<E> {

    @Override
    public void start() {
        super.start();
    }

    @Override
    protected void append(E eventObject) {

        // Not job context
        if (!(eventObject instanceof LoggingEvent)
                || Objects.isNull(EasyRetryLogManager.getLogMeta())
                || Objects.isNull(MDC.get(LogFieldConstants.MDC_REMOTE))) {
            return;
        }

        MDC.remove(LogFieldConstants.MDC_REMOTE);
        LogContentDTO logContentDTO = new LogContentDTO();

        // Prepare processing
        ((LoggingEvent) eventObject).prepareForDeferredProcessing();
        LoggingEvent event = (LoggingEvent) eventObject;

        logContentDTO.addTimeStamp(event.getTimeStamp());
        logContentDTO.addLevelField(event.getLevel().levelStr);
        logContentDTO.addThreadField(event.getThreadName());
        logContentDTO.addMessageField(event.getFormattedMessage());
        logContentDTO.addLocationField(getLocationField(event));
        logContentDTO.addThrowableField(getThrowableField(event));
        logContentDTO.addHostField(NettyChannel.getClientHost());
        logContentDTO.addPortField(NettyChannel.getClientPort());

        // slidingWindow syncReportLog
        Optional.ofNullable(LogReportFactory.get()).ifPresent(logReport -> logReport.report(logContentDTO));
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
        int stackDeep = 0;
        for (StackTraceElementProxy step : stackTraceElementProxyArray) {
            builder.append(CoreConstants.LINE_SEPARATOR);
            String string = step.toString();
            builder.append(CoreConstants.TAB).append(string);
            ThrowableProxyUtil.subjoinPackagingData(builder, step);
            // 最多显示30行
            if (++stackDeep >= 30) {
                break;
            }
        }
        return builder.toString();
    }

    private String getExceptionInfo(IThrowableProxy iThrowableProxy) {
        String s = iThrowableProxy.getClassName();
        String message = iThrowableProxy.getMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
