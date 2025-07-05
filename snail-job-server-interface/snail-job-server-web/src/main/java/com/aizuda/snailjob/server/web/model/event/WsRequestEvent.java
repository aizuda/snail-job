package com.aizuda.snailjob.server.web.model.event;

import com.aizuda.snailjob.server.common.enums.WebSocketSceneEnum;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.server.web.model.request
 * @Project：snail-job
 * @Date：2025/3/5 16:54
 * @Filename：WebSocketRequestVO
 * @since 1.5.0
 */
@Setter
@Getter
public class WsRequestEvent extends ApplicationEvent {
    /**
     * wb类型
     */
    private String sid;

    /**
     * context
     */
    private String message;

    private WebSocketSceneEnum sceneEnum;

    public WsRequestEvent(Object source) {
        super(source);
    }
}
