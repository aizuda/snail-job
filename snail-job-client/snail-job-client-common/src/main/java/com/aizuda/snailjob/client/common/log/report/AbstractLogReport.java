package com.aizuda.snailjob.client.common.log.report;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.LogReport;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.window.SlidingRingWindow;
import com.aizuda.snailjob.model.request.LogTaskRequest;
import com.aizuda.snailjob.common.core.window.Listener;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.common.log.dto.LogContentDTO;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * @author xiaowoniu
 * @date 2024-03-20 22:56:53
 * @since 3.2.0
 */
public abstract class AbstractLogReport<T extends LogTaskRequest> implements Lifecycle, InitializingBean, LogReport {

    @Autowired
    private SnailJobProperties snailJobProperties;
    private SlidingRingWindow<LogTaskRequest> slidingWindow;

    @Override
    public void report(LogContentDTO logContentDTO) {
        slidingWindow.add(buildLogTaskDTO(logContentDTO));
    }

    protected abstract T buildLogTaskDTO(LogContentDTO logContentDTO);

    @Override
    public void start() {
        if (Objects.nonNull(slidingWindow)) {
            return;
        }

        SnailJobProperties.LogSlidingWindowConfig logSlidingWindow = snailJobProperties.getLogSlidingWindow();

        Listener<LogTaskRequest> reportLogListener = new ReportLogListener();
        ChronoUnit chronoUnit = logSlidingWindow.getChronoUnit();
        Duration duration = Duration.of(logSlidingWindow.getDuration(), chronoUnit);
        slidingWindow= new SlidingRingWindow<>(duration, logSlidingWindow.getTotalThreshold(), Lists.newArrayList(reportLogListener));
    }

    @Override
    public void close() {
        if (Objects.isNull(slidingWindow)) {
            return;
        }

        SnailJobLog.LOCAL.info("AsyncReport Log about to shutdown");
        slidingWindow.shutdown();
        SnailJobLog.LOCAL.info("AsyncReport Log has been shutdown");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LogReportFactory.add(this);
    }
}
