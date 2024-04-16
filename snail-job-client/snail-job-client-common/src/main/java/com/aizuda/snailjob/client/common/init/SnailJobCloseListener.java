package com.aizuda.snailjob.client.common.init;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.event.SnailClientClosedEvent;
import com.aizuda.snailjob.client.common.event.SnailClientClosingEvent;
import com.aizuda.snailjob.common.core.context.SpringContext;
import com.aizuda.snailjob.common.core.util.SnailJobVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统关闭监听器
 *
 * @author: opensnail
 * @date : 2021-11-19 19:00
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SnailJobCloseListener implements ApplicationListener<ContextClosedEvent> {
    private final List<Lifecycle> lifecycleList;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("snail-job client about to shutdown v{}", SnailJobVersion.getVersion());
        SpringContext.getContext().publishEvent(new SnailClientClosingEvent());
        lifecycleList.forEach(Lifecycle::close);
        SpringContext.getContext().publishEvent(new SnailClientClosedEvent());
        log.info("snail-job client closed successfully v{}", SnailJobVersion.getVersion());
    }
}
