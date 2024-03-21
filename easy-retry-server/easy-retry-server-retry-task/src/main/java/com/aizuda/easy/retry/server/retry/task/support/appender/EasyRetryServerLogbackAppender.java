package com.aizuda.easy.retry.server.retry.task.support.appender;

import akka.actor.ActorRef;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.log.constant.LogFieldConstants;
import com.aizuda.easy.retry.common.log.dto.LogContentDTO;
import com.aizuda.easy.retry.common.log.dto.TaskLogFieldDTO;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.retry.task.dto.LogMetaDTO;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.log.RetryTaskLogDTO;
import com.google.common.collect.Lists;
import org.slf4j.MDC;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author xiaowoniu
 * @date 2023-12-27
 * @since 3.2.0
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
        RetryTaskLogDTO jobLogDTO = new RetryTaskLogDTO();
        Map<String, String> messageMap = logContentDTO.getFieldList()
            .stream()
            .filter(logTaskDTO_ -> !Objects.isNull(logTaskDTO_.getValue()))
            .collect(Collectors.toMap(TaskLogFieldDTO::getName, TaskLogFieldDTO::getValue));
        jobLogDTO.setMessage(JsonUtil.toJsonString(Lists.newArrayList(messageMap)));
        jobLogDTO.setGroupName(logMetaDTO.getGroupName());
        jobLogDTO.setNamespaceId(logMetaDTO.getNamespaceId());
        jobLogDTO.setUniqueId(logMetaDTO.getUniqueId());
        jobLogDTO.setRealTime(logMetaDTO.getTimestamp());
        ActorRef actorRef = ActorGenerator.jobLogActor();
        actorRef.tell(jobLogDTO, actorRef);
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
