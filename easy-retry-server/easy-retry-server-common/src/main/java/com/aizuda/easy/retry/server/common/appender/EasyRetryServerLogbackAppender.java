package com.aizuda.easy.retry.server.common.appender;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.core.util.NetUtil;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.log.constant.LogFieldConstants;
import com.aizuda.easy.retry.common.log.dto.LogContentDTO;
import com.aizuda.easy.retry.server.common.LogStorage;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.dto.LogMetaDTO;
import com.aizuda.easy.retry.server.common.log.LogStorageFactory;
import org.slf4j.MDC;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wodeyangzipingpingwuqi
 * @date 2023-12-27
 * @since 2.6.0
 */
public class EasyRetryServerLogbackAppender<E> extends UnsynchronizedAppenderBase<E> {

    @Override
    protected void append(E eventObject) {

        // Not job context
        if (!(eventObject instanceof LoggingEvent) || Objects.isNull(
            MDC.getMDCAdapter().get(LogFieldConstants.MDC_REMOTE))) {
            return;
        }

        MDC.getMDCAdapter().remove(LogFieldConstants.MDC_REMOTE);
        // Prepare processing
        ((LoggingEvent) eventObject).prepareForDeferredProcessing();
        LoggingEvent event = (LoggingEvent) eventObject;

        LogContentDTO logContentDTO = new LogContentDTO();
        logContentDTO.addLevelField(event.getLevel().levelStr);
        logContentDTO.addThreadField(event.getThreadName());
        logContentDTO.addLocationField(getLocationField(event));
        logContentDTO.addThrowableField(getThrowableField(event));
        logContentDTO.addHostField(NetUtil.getLocalIpStr());
        logContentDTO.addPortField(SpringContext.getBean(SystemProperties.class).getNettyPort());

        LogMetaDTO logMetaDTO = null;
        try {
            String patternString = "<\\|>(.*?)<\\|>";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(event.getFormattedMessage());
            while (matcher.find()) {
                String extractedData = matcher.group(1);
                if (StrUtil.isBlank(extractedData)) {
                    continue;
                }

                logMetaDTO = JsonUtil.parseObject(extractedData, LogMetaDTO.class);
                String message = event.getFormattedMessage().replaceFirst(patternString, StrUtil.EMPTY);
                logContentDTO.addMessageField(message);
                logContentDTO.addTimeStamp(Optional.ofNullable(logMetaDTO.getTimestamp()).orElse(event.getTimeStamp()));
                break;
            }

        } catch (Exception e) {
            EasyRetryLog.LOCAL.error("日志解析失败. msg:[{}]", event.getFormattedMessage(), e);
        }

        if (Objects.isNull(logMetaDTO)) {
            return;
        }

        // 保存执行的日志
        saveLog(logContentDTO, logMetaDTO);
    }

    /**
     * 保存日志
     *
     * @param logContentDTO 日志内容
     * @param logMetaDTO 日志元数据
     */
    private void saveLog(final LogContentDTO logContentDTO, final LogMetaDTO logMetaDTO) {

        LogStorage logStorage = LogStorageFactory.get(logMetaDTO.getLogType());
        if (Objects.nonNull(logStorage)) {
            logStorage.storage(logContentDTO, logMetaDTO);
        }
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
