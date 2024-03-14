package com.aizuda.easy.retry.client.common.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author xiaowoniu
 * @date 2024-03-14 21:17:55
 * @since 3.1.1
 */
public class ChannelReconnectEvent extends ApplicationEvent {
    private static final String SOURCE = "ChannelReconnect";
    public ChannelReconnectEvent() {
        super(SOURCE);
    }
}
