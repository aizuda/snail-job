package com.aizuda.snail.job.client.common.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author xiaowoniu
 * @date 2024-03-14 21:17:55
 * @since 3.1.1
 */
public class SnailChannelReconnectEvent extends ApplicationEvent {
    private static final String SOURCE = "ChannelReconnect";
    public SnailChannelReconnectEvent() {
        super(SOURCE);
    }
}
