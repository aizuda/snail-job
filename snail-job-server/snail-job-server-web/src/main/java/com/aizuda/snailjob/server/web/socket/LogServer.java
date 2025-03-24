package com.aizuda.snailjob.server.web.socket;

import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.server.common.enums.WebSocketSceneEnum;
import com.aizuda.snailjob.server.common.vo.WsRequestVO;
import com.aizuda.snailjob.server.web.config.WebSocketConfigurator;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.server.web.socket
 * @Project：snail-job
 * @Date：2025/3/4 16:31
 * @Filename：LogServer
 * @since 1.5.0
 */
@Slf4j
@Component
@ServerEndpoint(value = "/webSocket", configurator = WebSocketConfigurator.class)
public class LogServer {

    // 缓存session
    public static final ConcurrentHashMap<String, Session> USER_SESSION = new ConcurrentHashMap<>();


    @OnOpen
    public void onOpen(Session session) {
        Map<String, Object> userProperties = session.getUserProperties();
        USER_SESSION.put((String) userProperties.get(WebSocketConfigurator.SID), session);
    }


    @OnMessage
    public void onMessage(String message, Session session) throws IOException, EncodeException {
        // 接收请求
        Map<String, Object> userProperties = session.getUserProperties();
        String sid = (String) userProperties.get(WebSocketConfigurator.SID);
        String scene = (String) userProperties.get(WebSocketConfigurator.SCENE);
        WsRequestVO requestVO = new WsRequestVO();
        requestVO.setSceneEnum(WebSocketSceneEnum.valueOf(scene));
        requestVO.setMessage(message);
        requestVO.setSid(sid);
        SnailSpringContext.getContext().publishEvent(requestVO);
    }

    //错误时调用
    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("发生错误{}", throwable);
    }

}
