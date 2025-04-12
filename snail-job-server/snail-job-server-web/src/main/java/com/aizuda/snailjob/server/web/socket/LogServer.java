package com.aizuda.snailjob.server.web.socket;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.server.common.enums.WebSocketSceneEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.web.model.event.WsSendEvent;
import com.aizuda.snailjob.server.web.model.event.WsRequestEvent;
import com.aizuda.snailjob.server.web.config.WebSocketConfigurator;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
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
@ServerEndpoint(value = "/websocket", configurator = WebSocketConfigurator.class)
public class LogServer {

    // 缓存session
    public static final ConcurrentHashMap<String, Session> USER_SESSION = new ConcurrentHashMap<>();

    @EventListener
    public void sendMessage(WsSendEvent message) throws IOException {
        Session session = USER_SESSION.get(message.getSid());
        Assert.notNull(session, () -> new SnailJobServerException("ws session not exist"));
        if (session.isOpen()) {
            synchronized (session) {
                session.getBasicRemote().sendText(message.getMessage());
            }
        }

    }

    @OnOpen
    public void onOpen(Session session) {
        Map<String, Object> userProperties = session.getUserProperties();
        String sid = (String) userProperties.get(WebSocketConfigurator.SID);
        USER_SESSION.put(sid, session);
        log.info("sid:[{}] websocket started", sid);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 接收请求
        Map<String, Object> userProperties = session.getUserProperties();
        String sid = (String) userProperties.get(WebSocketConfigurator.SID);
        String scene = (String) userProperties.get(WebSocketConfigurator.SCENE);
        WsRequestEvent requestVO = new WsRequestEvent(this);
        requestVO.setSceneEnum(WebSocketSceneEnum.valueOf(scene));
        requestVO.setMessage(message);
        requestVO.setSid(sid);
        SnailSpringContext.getContext().publishEvent(requestVO);
    }


    /**
     * 连接关闭时触发
     */
    @OnClose
    public void onClose(Session session) {

        Map<String, Object> userProperties = session.getUserProperties();
        String sid = (String) userProperties.get(WebSocketConfigurator.SID);

        log.info("sid:[{}] websocket closed", sid);
        USER_SESSION.remove(sid);
    }

    //错误时调用
    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("Error occurred {}", throwable);
    }

}
