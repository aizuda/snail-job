package com.aizuda.snailjob.client.core.report;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.window.SlidingWindow;
import com.aizuda.snailjob.client.core.retryer.RetryerInfo;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.model.dto.RetryTaskDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 异步上报数据
 *
 * @author opensnail
 * @date 2023-05-15
 * @since 1.3.0
 */
@Component
public class AsyncReport extends AbstractReport implements Lifecycle {
    private SlidingWindow<RetryTaskDTO> slidingWindow;
    @Override
    public boolean supports(boolean async) {
        return async;
    }

    @Override
    public boolean doReport(RetryerInfo retryerInfo, Object[] params) {

        return syncReport(retryerInfo.getScene(), retryerInfo.getExecutorClassName(), params, retryerInfo.getTimeout(),
            retryerInfo.getUnit());
    }

    /**
     * 异步上报到服务端, 若当前处于远程重试阶段不会进行执行上报
     */
    public Boolean syncReport(String scene, String targetClassName, Object[] args, long timeout, TimeUnit unit) {

        RetryTaskDTO retryTaskDTO = buildRetryTaskDTO(scene, targetClassName, args);
        slidingWindow.add(retryTaskDTO);
        return Boolean.TRUE;
    }

    @Override
    public void start() {

        SnailJobProperties.SlidingWindowConfig slidingWindowConfig = snailJobProperties.getRetry().getReportSlidingWindow();

        slidingWindow = SlidingWindow
            .Builder
            .<RetryTaskDTO>newBuilder()
            .withTotalThreshold(slidingWindowConfig.getTotalThreshold())
            .withWindowTotalThreshold(slidingWindowConfig.getWindowTotalThreshold())
            .withDuration(slidingWindowConfig.getDuration(), slidingWindowConfig.getChronoUnit())
            .withListener(new ReportListener())
            .build();

        slidingWindow.start();
    }

    @Override
    public void close() {
        SnailJobLog.LOCAL.info("AsyncReport about to shutdown");
        if (Objects.nonNull(slidingWindow)) {
            slidingWindow.end();
        }
        SnailJobLog.LOCAL.info("AsyncReport has been shutdown");
    }
}
