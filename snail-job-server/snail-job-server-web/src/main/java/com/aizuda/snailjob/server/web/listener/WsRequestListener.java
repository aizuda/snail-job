package com.aizuda.snailjob.server.web.listener;

import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.enums.WebSocketSceneEnum;
import com.aizuda.snailjob.server.common.service.LogService;
import com.aizuda.snailjob.server.common.vo.JobLogQueryVO;
import com.aizuda.snailjob.server.common.vo.WsRequestVO;
import jakarta.websocket.Session;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.aizuda.snailjob.server.web.socket.LogServer.USER_SESSION;

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
public class WsRequestListener {
    private final LogService logService;

    @Async
    @EventListener(classes = WsRequestVO.class)
    public void getJobLogs(WsRequestVO requestVO) {
        if (!WebSocketSceneEnum.JOB_LOG_SCENE.equals(requestVO.getSceneEnum())) {
            return;
        }
        String message = requestVO.getMessage();
        JobLogQueryVO jobLogQueryVO = JsonUtil.parseObject(message, JobLogQueryVO.class);
        Session session = USER_SESSION.get(requestVO.getSid());
        try {
            logService.getJobLogPage(jobLogQueryVO, session);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
