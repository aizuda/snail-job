package com.aizuda.easy.retry.server.starter.listener;

import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.core.util.EasyRetryVersion;
import com.aizuda.easy.retry.server.common.Lifecycle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统启动监听器
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-19 19:00
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StartListener implements ApplicationListener<ContextRefreshedEvent> {
    private final List<Lifecycle> lifecycleList;
    private volatile boolean isStarted = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (isStarted) {
            EasyRetryLog.LOCAL.info("Easy-Retry server already started v{}", EasyRetryVersion.getVersion());
            return;
        }

        System.out.println(MessageFormatter.format(SystemConstants.LOGO, EasyRetryVersion.getVersion()).getMessage());
        EasyRetryLog.LOCAL.info("Easy-Retry server is preparing to start... v{}", EasyRetryVersion.getVersion());
        lifecycleList.forEach(Lifecycle::start);
        EasyRetryLog.LOCAL.info("Easy-Retry server started successfully v{}", EasyRetryVersion.getVersion());
        isStarted = true;
    }
}
