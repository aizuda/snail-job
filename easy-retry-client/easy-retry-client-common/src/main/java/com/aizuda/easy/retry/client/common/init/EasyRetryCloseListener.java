package com.aizuda.easy.retry.client.common.init;

import com.aizuda.easy.retry.client.common.Lifecycle;
import com.aizuda.easy.retry.client.common.event.EasyRetryClosedEvent;
import com.aizuda.easy.retry.client.common.event.EasyRetryClosingEvent;
import com.aizuda.easy.retry.client.common.event.EasyRetryStartingEvent;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.util.EasyRetryVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统关闭监听器
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-19 19:00
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EasyRetryCloseListener implements ApplicationListener<ContextClosedEvent> {
    private final List<Lifecycle> lifecycleList;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("Easy-Retry client about to shutdown v{}", EasyRetryVersion.getVersion());
        SpringContext.getContext().publishEvent(new EasyRetryClosingEvent());
        lifecycleList.forEach(Lifecycle::close);
        SpringContext.getContext().publishEvent(new EasyRetryClosedEvent());
        log.info("Easy-Retry client closed successfully v{}", EasyRetryVersion.getVersion());
    }
}
