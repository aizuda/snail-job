package com.aizuda.easy.retry.client.core.report;

import com.aizuda.easy.retry.client.core.Lifecycle;
import com.aizuda.easy.retry.client.core.retryer.RetryerInfo;
import com.aizuda.easy.retry.client.core.window.RetryLeapArray;
import com.aizuda.easy.retry.client.core.window.SlidingWindow;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 异步上报数据
 *
 * @author www.byteblogs.com
 * @date 2023-05-15
 * @since 1.3.0
 */
@Component
@Slf4j
public class AsyncReport extends AbstractReport implements Lifecycle {
    private static SlidingWindow<RetryTaskDTO> slidingWindow;

    private static ScheduledExecutorService dispatchService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "DispatchService"));

//    public static RetryLeapArray slidingWindow = new RetryLeapArray(SAMPLE_COUNT, INTERVAL_IN_MS, new ReportListener());

    @Override
    public boolean supports(boolean async) {
        return async;
    }

    @Override
    public boolean doReport(RetryerInfo retryerInfo, Object[] params) {

        return syncReport(retryerInfo.getScene(), retryerInfo.getExecutorClassName(), params, retryerInfo.getTimeout(), retryerInfo.getUnit());
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

        slidingWindow = SlidingWindow
                .Builder
                .<RetryTaskDTO>newBuilder()
                .withTotalThreshold(50)
                .withDuration(5, ChronoUnit.SECONDS)
                .withListener(new ReportListener())
                .build();
        slidingWindow.start();
//        dispatchService.scheduleAtFixedRate(() -> {
//            slidingWindow.currentWindow();
//        }, INTERVAL_IN_MS, INTERVAL_IN_MS / SAMPLE_COUNT, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        log.info("AsyncReport about to shutdown");
        slidingWindow.end();
        log.info("AsyncReport has been shutdown");
    }
}
