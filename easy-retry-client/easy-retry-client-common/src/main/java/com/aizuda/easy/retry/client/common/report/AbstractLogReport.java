package com.aizuda.easy.retry.client.common.report;

import com.aizuda.easy.retry.client.common.Lifecycle;
import com.aizuda.easy.retry.client.common.config.EasyRetryProperties;
import com.aizuda.easy.retry.client.common.window.SlidingWindow;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.log.dto.LogContentDTO;
import com.aizuda.easy.retry.server.model.dto.LogTaskDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * @author xiaowoniu
 * @date 2024-03-20 22:56:53
 * @since 3.2.0
 */
public abstract class AbstractLogReport implements Lifecycle, LogReport {
    @Autowired
    private EasyRetryProperties easyRetryProperties;
    private SlidingWindow<? super LogTaskDTO> slidingWindow;
    @Override
    public void report(LogContentDTO logContentDTO) {
        slidingWindow.add(buildLogTaskDTO(logContentDTO));
    }

    protected abstract LogTaskDTO buildLogTaskDTO(LogContentDTO logContentDTO);

    @Override
    public void start() {

        EasyRetryProperties.LogSlidingWindowConfig logSlidingWindow = easyRetryProperties.getLogSlidingWindow();

        slidingWindow = SlidingWindow
                .Builder
                .<LogTaskDTO>newBuilder()
                .withTotalThreshold(logSlidingWindow.getTotalThreshold())
                .withWindowTotalThreshold(logSlidingWindow.getWindowTotalThreshold())
                .withDuration(logSlidingWindow.getDuration(), logSlidingWindow.getChronoUnit())
                .withListener(new ReportLogListener())
                .build();

        slidingWindow.start();
    }

    @Override
    public void close() {
        EasyRetryLog.LOCAL.info("AsyncReport Log about to shutdown");
        if (Objects.nonNull(slidingWindow)) {
            slidingWindow.end();
        }
        EasyRetryLog.LOCAL.info("AsyncReport Log has been shutdown");
    }
}
