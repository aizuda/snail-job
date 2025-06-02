package com.aizuda.snailjob.client.core.report;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.window.SlidingRingWindow;
import com.aizuda.snailjob.client.core.retryer.RetryerInfo;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.model.dto.RetryTaskDTO;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
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
    private SlidingRingWindow<RetryTaskDTO> slidingWindow;

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

        ChronoUnit chronoUnit = slidingWindowConfig.getChronoUnit();
        Duration duration = Duration.of(slidingWindowConfig.getDuration(), chronoUnit);
        slidingWindow= new SlidingRingWindow<>(duration, slidingWindowConfig.getTotalThreshold(), Lists.newArrayList(new ReportListener()));

    }

    @Override
    public void close() {
        SnailJobLog.LOCAL.info("AsyncReport about to shutdown");
        if (Objects.nonNull(slidingWindow)) {
            slidingWindow.shutdown();
        }
        SnailJobLog.LOCAL.info("AsyncReport has been shutdown");
    }
}
