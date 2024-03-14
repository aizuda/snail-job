package com.aizuda.easy.retry.client.common.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author xiaowoniu
 * @date 2024-03-14 21:23:29
 * @since 3.1.0
 */
public class EasyRetryClosingEvent extends ApplicationEvent {
    private static final String SOURCE = "EasyRetryClosing";

    public EasyRetryClosingEvent() {
        super(SOURCE);
    }
}
