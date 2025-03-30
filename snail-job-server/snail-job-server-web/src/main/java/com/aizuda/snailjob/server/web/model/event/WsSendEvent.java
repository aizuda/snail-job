package com.aizuda.snailjob.server.web.model.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-01-18
 */
@Setter
@Getter
public class WsSendEvent extends ApplicationEvent {

    /**
     * 会话id
     */
    private String sid;

    /**
     * 需要发送的消息
     */
    private String message;

    public WsSendEvent(Object source) {
        super(source);
    }
}
