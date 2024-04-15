package com.aizuda.snail.job.client.common.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author: opensnail
 * @date : 2024-04-15
 * @since : 3.3.0
 */
public class SnailServerStartFailedEvent extends ApplicationEvent {

    private static final String SOURCE = "SnailServerStartFailed";

    public SnailServerStartFailedEvent() {
        super(SOURCE);
    }

}
