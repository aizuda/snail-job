package com.aizuda.easy.retry.client.common.appender;

import com.aizuda.easy.retry.client.common.log.report.LogReportFactory;
import com.aizuda.easy.retry.client.common.log.support.EasyRetryLogManager;
import com.aizuda.easy.retry.client.common.netty.NettyChannel;
import com.aizuda.easy.retry.common.log.dto.LogContentDTO;
import com.aizuda.easy.retry.common.log.constant.LogFieldConstants;
import org.apache.log4j.MDC;
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

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * @author wodeyangzipingpingwuqi
 * @date 2024-01-02
 * @since 2.6.0
 */
@Plugin(name = "EasyRetryLog4j2Appender", category = "Core", elementType = "appender", printObject = true)
public class EasyRetryLog4j2Appender extends AbstractAppender {

    @Override
    public void append(LogEvent event) {

        // Not job context
        if (Objects.isNull(EasyRetryLogManager.getLogMeta()) || Objects.isNull(MDC.get(LogFieldConstants.MDC_REMOTE))) {
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

    protected EasyRetryLog4j2Appender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
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
    public static EasyRetryLog4j2Appender create(
            @PluginAttribute("name") final String name,
            @PluginElement("Filter") final Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute("ignoreExceptions") final String ignore,
            @PluginAttribute("timeFormat") final String timeFormat,
            @PluginAttribute("timeZone") final String timeZone
    ) {
        boolean ignoreExceptions = Booleans.parseBoolean(ignore, false);
        EasyRetryLog4j2Appender log4j2Appender = new EasyRetryLog4j2Appender(name, filter, layout, ignoreExceptions);
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
        for (String s : Throwables.toStringList(throwable)) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(System.getProperty("line.separator"));
            }
            sb.append(s);
        }
        return sb.toString();
    }
}
