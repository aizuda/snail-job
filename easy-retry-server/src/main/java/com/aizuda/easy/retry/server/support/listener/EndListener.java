package com.aizuda.easy.retry.server.support.listener;

import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.support.Lifecycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 关闭监听器
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
        LogUtils.info(log, "EASY-RETRY-SERVER 停止");
        lifecycleList.forEach(Lifecycle::close);
    }
}
