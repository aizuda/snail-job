package com.aizuda.snailjob.server.web.listener;

import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.enums.WebSocketSceneEnum;
import com.aizuda.snailjob.server.web.model.request.JobLogQueryVO;
import com.aizuda.snailjob.server.web.model.event.WsRequestEvent;
import com.aizuda.snailjob.server.web.model.request.RetryTaskLogMessageQueryVO;
import com.aizuda.snailjob.server.web.service.JobLogService;
import com.aizuda.snailjob.server.web.service.RetryTaskService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.server.web.listener
 * @Project：snail-job
 * @Date：2025/3/18 10:56
 * @Filename：WsRequestListener
 * @since 1.5.0
 */
@Component
@AllArgsConstructor
@Slf4j
public class WsRequestListener {
    private final JobLogService jobLogService;

    private final RetryTaskService retryTaskService;

    @Async("logQueryExecutor")
    @EventListener(classes = WsRequestEvent.class)
    public void getJobLogs(WsRequestEvent requestVO) {
        if (!WebSocketSceneEnum.JOB_LOG_SCENE.equals(requestVO.getSceneEnum())) {
            return;
        }

        log.info("getJobLogs {}", requestVO.getSid());
        String message = requestVO.getMessage();
        JobLogQueryVO jobLogQueryVO = JsonUtil.parseObject(message, JobLogQueryVO.class);
        jobLogQueryVO.setSid(requestVO.getSid());
        jobLogQueryVO.setStartRealTime(0L);
        try {
            jobLogService.getJobLogPage(jobLogQueryVO);
        } catch (Exception e) {
            log.warn("send log error", e);
        }

    }


    @Async("logQueryExecutor")
    @EventListener(classes = WsRequestEvent.class)
    public void getRetryLogs(WsRequestEvent requestVO) {
        if (!WebSocketSceneEnum.RETRY_LOG_SCENE.equals(requestVO.getSceneEnum())) {
            return;
        }

        log.info("getRetryLogs {}", requestVO.getSid());
        String message = requestVO.getMessage();
        RetryTaskLogMessageQueryVO retryTaskLogMessageQueryVO = JsonUtil.parseObject(message, RetryTaskLogMessageQueryVO.class);
        retryTaskLogMessageQueryVO.setSid(requestVO.getSid());
        retryTaskLogMessageQueryVO.setStartRealTime(0L);
        try {
            retryTaskService.getRetryTaskLogMessagePage(retryTaskLogMessageQueryVO);
        } catch (Exception e) {
            log.warn("send log error", e);
        }

    }
}
