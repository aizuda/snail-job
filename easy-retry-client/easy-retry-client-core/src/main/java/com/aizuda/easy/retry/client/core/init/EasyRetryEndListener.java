package com.aizuda.easy.retry.client.core.init;

import com.aizuda.easy.retry.client.core.Lifecycle;
import com.aizuda.easy.retry.common.core.util.EasyRetryVersion;
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
@Slf4j
public class EasyRetryEndListener implements ApplicationListener<ContextClosedEvent> {

    @Autowired
    private List<Lifecycle> lifecycleList;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("Easy-Retry client about to shutdown v{}", EasyRetryVersion.getVersion());
        lifecycleList.forEach(Lifecycle::close);
        log.info("Easy-Retry client closed successfully v{}", EasyRetryVersion.getVersion());
    }
}
