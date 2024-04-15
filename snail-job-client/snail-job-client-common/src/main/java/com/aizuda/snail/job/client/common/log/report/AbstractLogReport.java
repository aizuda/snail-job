package com.aizuda.snail.job.client.common.log.report;

import com.aizuda.snail.job.client.common.Lifecycle;
import com.aizuda.snail.job.client.common.LogReport;
import com.aizuda.snail.job.client.common.config.SnailJobProperties;
import com.aizuda.snail.job.client.common.window.SlidingWindow;
import com.aizuda.snail.job.common.core.window.Listener;
import com.aizuda.snail.job.common.log.EasyRetryLog;
import com.aizuda.snail.job.common.log.dto.LogContentDTO;
import com.aizuda.snail.job.server.model.dto.LogTaskDTO;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * @author xiaowoniu
 * @date 2024-03-20 22:56:53
 * @since 3.2.0
 */
public abstract class AbstractLogReport<T extends LogTaskDTO> implements Lifecycle, InitializingBean, LogReport {

    @Autowired
    private SnailJobProperties snailJobProperties;
    private SlidingWindow<LogTaskDTO> slidingWindow;
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

        Listener<LogTaskDTO> reportLogListener = new ReportLogListener();
        slidingWindow = SlidingWindow
                .Builder
                .<LogTaskDTO>newBuilder()
                .withTotalThreshold(logSlidingWindow.getTotalThreshold())
                .withWindowTotalThreshold(logSlidingWindow.getWindowTotalThreshold())
                .withDuration(logSlidingWindow.getDuration(), logSlidingWindow.getChronoUnit())
                .withListener(reportLogListener)
                .build();

        slidingWindow.start();
    }

    @Override
    public void close() {
        if (Objects.isNull(slidingWindow)) {
            return;
        }

        EasyRetryLog.LOCAL.info("AsyncReport Log about to shutdown");
        slidingWindow.end();
        EasyRetryLog.LOCAL.info("AsyncReport Log has been shutdown");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LogReportFactory.add(this);
    }
}
