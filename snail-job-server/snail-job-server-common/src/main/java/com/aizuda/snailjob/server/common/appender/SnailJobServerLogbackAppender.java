package com.aizuda.snailjob.server.common.appender;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.NetUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.common.log.constant.LogFieldConstants;
import com.aizuda.snailjob.common.log.dto.LogContentDTO;
import com.aizuda.snailjob.common.log.enums.LogTypeEnum;
import com.aizuda.snailjob.server.common.LogStorage;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.JobLogMetaDTO;
import com.aizuda.snailjob.server.common.dto.LogMetaDTO;
import com.aizuda.snailjob.server.common.dto.RetryLogMetaDTO;
import com.aizuda.snailjob.server.common.log.LogStorageFactory;
import com.fasterxml.jackson.databind.JsonNode;
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
public class SnailJobServerLogbackAppender<E> extends UnsynchronizedAppenderBase<E> {

    @Override
    protected void append(E eventObject) {

        // Not job context
        if (!(eventObject instanceof LoggingEvent event) || Objects.isNull(
                MDC.getMDCAdapter().get(LogFieldConstants.MDC_REMOTE))) {
            return;
        }

        MDC.getMDCAdapter().remove(LogFieldConstants.MDC_REMOTE);
        // Prepare processing
        event.prepareForDeferredProcessing();

        LogContentDTO logContentDTO = new LogContentDTO();
        logContentDTO.addLevelField(event.getLevel().levelStr);
        logContentDTO.addThreadField(event.getThreadName());
        logContentDTO.addLocationField(getLocationField(event));
        logContentDTO.addThrowableField(getThrowableField(event));
        logContentDTO.addHostField(NetUtil.getLocalIpStr());
        logContentDTO.addPortField(SnailSpringContext.getBean(SystemProperties.class).getServerPort());

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

                JsonNode jsonNode = JsonUtil.toJson(extractedData);
                if (!jsonNode.has(SystemConstants.JSON_FILED_LOG_TYPE)) {
                    return;
                }

                String name = jsonNode.get(SystemConstants.JSON_FILED_LOG_TYPE).asText();
                if (LogTypeEnum.RETRY.equals(LogTypeEnum.valueOf(name))) {
                    logMetaDTO = JsonUtil.parseObject(extractedData, RetryLogMetaDTO.class);
                } else if (LogTypeEnum.JOB.equals(LogTypeEnum.valueOf(name))) {
                    logMetaDTO = JsonUtil.parseObject(extractedData, JobLogMetaDTO.class);
                } else {
                    throw new IllegalArgumentException("logType is not support");
                }

                String message = event.getFormattedMessage().replaceFirst(patternString, StrUtil.EMPTY);
                logContentDTO.addMessageField(message);
                logContentDTO.addTimeStamp(Optional.ofNullable(logMetaDTO.getTimestamp()).orElse(event.getTimeStamp()));
                break;
            }

            if (Objects.isNull(logMetaDTO)) {
                return;
            }

            // 保存执行的日志
            saveLog(logContentDTO, logMetaDTO);

        } catch (Exception e) {
            SnailJobLog.LOCAL.error("日志解析失败. msg:[{}]", event.getFormattedMessage(), e);
        }

    }

    /**
     * 保存日志
     *
     * @param logContentDTO 日志内容
     * @param logMetaDTO    日志元数据
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
