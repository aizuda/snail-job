package com.aizuda.easy.retry.client.common.log.report;

import com.aizuda.easy.retry.client.common.Lifecycle;
import com.aizuda.easy.retry.client.common.LogReport;
import com.aizuda.easy.retry.client.common.config.EasyRetryProperties;
import com.aizuda.easy.retry.client.common.window.SlidingWindow;
import com.aizuda.easy.retry.common.core.window.Listener;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.log.dto.LogContentDTO;
import com.aizuda.easy.retry.server.model.dto.LogTaskDTO;
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
    private EasyRetryProperties easyRetryProperties;
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

        EasyRetryProperties.LogSlidingWindowConfig logSlidingWindow = easyRetryProperties.getLogSlidingWindow();

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
