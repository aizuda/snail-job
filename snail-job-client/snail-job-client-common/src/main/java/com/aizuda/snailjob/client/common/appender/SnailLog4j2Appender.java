/*
 * Copyright (c) 2024 .
 *
 * SnailJob - 灵活，可靠和快速的分布式任务重试和分布式任务调度平台
 * > ✅️ 可重放，可管控、为提高分布式业务系统一致性的分布式任务重试平台
 * > ✅️ 支持秒级、可中断、可编排的高性能分布式任务调度平台
 *
 * Aizuda/SnailJob 采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点:
 *
 *
 * 1. 不得修改产品相关代码的源码头注释和出处;
 * 2. 不得应用于危害国家安全、荣誉和利益的行为，不能以任何形式用于非法目的;
 *
 */
package com.aizuda.snailjob.client.common.appender;

import com.aizuda.snailjob.client.common.log.report.LogReportFactory;
import com.aizuda.snailjob.client.common.log.support.SnailJobLogManager;
import com.aizuda.snailjob.client.common.rpc.client.NettyChannel;
import com.aizuda.snailjob.common.log.constant.LogFieldConstants;
import com.aizuda.snailjob.common.log.dto.LogContentDTO;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.util.Throwables;
import org.slf4j.MDC;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * @author wodeyangzipingpingwuqi
 * @date 2024-01-02
 * @since 2.6.0
 */
@Plugin(name = "SnailLog4j2Appender", category = "Core", elementType = "appender", printObject = true)
public class SnailLog4j2Appender extends AbstractAppender {

    @Override
    public void append(LogEvent event) {

        // Not job context
        if (Objects.isNull(SnailJobLogManager.getLogMeta()) || Objects.isNull(MDC.get(LogFieldConstants.MDC_REMOTE))) {
            return;
        }

        MDC.remove(LogFieldConstants.MDC_REMOTE);
        LogContentDTO logContentDTO = new LogContentDTO();
        logContentDTO.addTimeStamp(event.getTimeMillis());
        logContentDTO.addLevelField(event.getLevel().name());
        logContentDTO.addThreadField(event.getThreadName());
        logContentDTO.addLocationField(getLocationField(event));
        logContentDTO.addThrowableField(getThrowableField(event));
        logContentDTO.addMessageField(event.getMessage().getFormattedMessage());
        logContentDTO.addHostField(NettyChannel.getClientHost());
        logContentDTO.addPortField(NettyChannel.getClientPort());

        // slidingWindow syncReportLog
        Optional.ofNullable(LogReportFactory.get()).ifPresent(logReport -> logReport.report(logContentDTO));
    }

    protected SnailLog4j2Appender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
    }

    /**
     * Create Log4j2Appender
     *
     * @param name       name
     * @param filter     filter
     * @param layout     layout
     * @param ignore     ignore
     * @param timeFormat time format
     * @param timeZone   time zone
     * @return Log4j2Appender
     */
    @PluginFactory
    public static SnailLog4j2Appender create(
            @PluginAttribute("name") final String name,
            @PluginElement("Filter") final Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute("ignoreExceptions") final String ignore,
            @PluginAttribute("timeFormat") final String timeFormat,
            @PluginAttribute("timeZone") final String timeZone
    ) {
        boolean ignoreExceptions = Booleans.parseBoolean(ignore, false);
        SnailLog4j2Appender log4j2Appender = new SnailLog4j2Appender(name, filter, layout, ignoreExceptions);
        return log4j2Appender;
    }

    private String getLocationField(LogEvent event) {
        StackTraceElement source = event.getSource();
        if (source == null && (!event.isIncludeLocation())) {
            event.setIncludeLocation(true);
            source = event.getSource();
            event.setIncludeLocation(false);
        }
        return source == null ? "Unknown(Unknown Source)" : source.toString();
    }

    private String getThrowableField(LogEvent event) {
        String throwable = getThrowableStr(event.getThrown());
        if (throwable != null) {
            return throwable;
        }
        return null;
    }

    private String getThrowableStr(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        int stackDeep = 0;
        for (String s : Throwables.toStringList(throwable)) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(System.getProperty("line.separator"));
            }
            sb.append(s);
            // 最多显示30行
            if (++stackDeep >= 30) {
                break;
            }
        }
        return sb.toString();
    }
}
