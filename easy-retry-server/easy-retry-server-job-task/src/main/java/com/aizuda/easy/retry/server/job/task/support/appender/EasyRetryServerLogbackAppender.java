package com.aizuda.easy.retry.server.job.task.support.appender;

import akka.actor.ActorRef;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.constant.LogFieldConstant;
import com.aizuda.easy.retry.common.core.log.LogContentDTO;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.job.task.dto.JobLogDTO;
import com.aizuda.easy.retry.server.job.task.dto.LogMetaDTO;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // Prepare processing
        ((LoggingEvent) eventObject).prepareForDeferredProcessing();
        LoggingEvent event = (LoggingEvent) eventObject;

        LogMetaDTO logMetaDTO = null;
        try {
           // 第一种是MDC
            String logMetaStr = MDC.getMDCAdapter().get(LogFieldConstant.LOG_META);
            if (StrUtil.isNotBlank(logMetaStr)) {
                logMetaDTO = JsonUtil.parseObject(logMetaStr, LogMetaDTO.class);
            } else {
                // 第二种规则是按照正则匹配
                String patternString = "<\\|>(.*?)<\\|>";
                Pattern pattern = Pattern.compile(patternString);
                Matcher matcher = pattern.matcher(event.getFormattedMessage());
                while (matcher.find()) {
                    String extractedData = matcher.group(1);
                    if (StrUtil.isBlank(extractedData)) {
                        continue;
                    }

                    logMetaDTO = JsonUtil.parseObject(extractedData, LogMetaDTO.class);
                }
            }

        } catch (Exception e) {
            EasyRetryLog.LOCAL.error("日志解析失败. msg:[{}]", event.getFormattedMessage(), e);
        } finally {
            MDC.getMDCAdapter().remove(LogFieldConstant.MDC_REMOTE);
            MDC.getMDCAdapter().remove(LogFieldConstant.LOG_META);
        }

        if (Objects.isNull(logMetaDTO)) {
            return;
        }

        // 保存执行的日志
        JobLogDTO jobLogDTO = new JobLogDTO();
        jobLogDTO.setMessage(event.getFormattedMessage());
        jobLogDTO.setTaskId(logMetaDTO.getTaskId());
        jobLogDTO.setJobId(logMetaDTO.getJobId());
        jobLogDTO.setGroupName(logMetaDTO.getGroupName());
        jobLogDTO.setNamespaceId(logMetaDTO.getNamespaceId());
        jobLogDTO.setTaskBatchId(logMetaDTO.getTaskBatchId());
        jobLogDTO.setRealTime(DateUtils.toNowMilli());
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
