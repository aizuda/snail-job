package com.aizuda.easy.retry.client.common.report;

import com.aizuda.easy.retry.client.common.Lifecycle;
import com.aizuda.easy.retry.client.common.Report;
import com.aizuda.easy.retry.client.common.config.EasyRetryProperties;
import com.aizuda.easy.retry.client.common.util.ThreadLocalLogUtil;
import com.aizuda.easy.retry.client.common.window.SlidingWindow;
import com.aizuda.easy.retry.common.core.model.JobContext;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.log.dto.LogContentDTO;
import com.aizuda.easy.retry.server.model.dto.LogTaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 异步上报日志数据
 *
 * @author wodeyangzipingpingwuqi
 * @date 2023-12-27
 * @since 2.6.0
 */
@Component
@Slf4j
public class AsyncReportLog implements Lifecycle {

    @Autowired
    private EasyRetryProperties easyRetryProperties;

    private SlidingWindow<LogTaskDTO> slidingWindow;

    /**
     * 异步上报到服务端, 若当前处于远程重试阶段不会进行执行上报
     */
    public Boolean syncReportLog(LogContentDTO logContent) {

        LogTaskDTO logTaskDTO = buildLogTaskDTO(logContent);
        slidingWindow.add(logTaskDTO);
        return Boolean.TRUE;
    }

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

    /**
     * 构建上报任务对象
     *
     * @return logContent 上报服务端对象
     */
    protected LogTaskDTO buildLogTaskDTO(LogContentDTO logContentDTO) {
        JobContext context = ThreadLocalLogUtil.getContext();

        LogTaskDTO logTaskDTO = new LogTaskDTO();
        logTaskDTO.setJobId(context.getJobId());
        logTaskDTO.setTaskId(context.getTaskId());
        logTaskDTO.setTaskBatchId(context.getTaskBatchId());
        logTaskDTO.setRealTime(logContentDTO.getTimeStamp());
        logTaskDTO.setNamespaceId(context.getNamespaceId());
        logTaskDTO.setGroupName(context.getGroupName());
        logTaskDTO.setFieldList(logContentDTO.getFieldList());
        return logTaskDTO;
    }
}
