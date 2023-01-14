package com.x.retry.client.core.init;

import com.x.retry.client.core.Lifecycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 重试对账系统关闭监听器
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-19 19:00
 */
@Component
@Slf4j
public class EndListener implements ApplicationListener<ContextClosedEvent> {

    @Autowired
    private List<Lifecycle> lifecycleList;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("X-RETRY-CLIENT 关闭");
        lifecycleList.forEach(Lifecycle::close);
        log.info("X-RETRY-CLIENT 关闭成功");
    }
}
