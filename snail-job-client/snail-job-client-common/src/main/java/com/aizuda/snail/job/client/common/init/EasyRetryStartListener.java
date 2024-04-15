package com.aizuda.snail.job.client.common.init;

import com.aizuda.snail.job.client.common.Lifecycle;
import com.aizuda.snail.job.client.common.event.SnailClientStartedEvent;
import com.aizuda.snail.job.client.common.event.SnailClientStartingEvent;
import com.aizuda.snail.job.common.core.constant.SystemConstants;
import com.aizuda.snail.job.common.core.context.SpringContext;
import com.aizuda.snail.job.common.core.util.EasyRetryVersion;
import com.aizuda.snail.job.common.log.EasyRetryLog;
import lombok.RequiredArgsConstructor;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统启动监听器
 *
 * @author: opensnail
 * @date : 2021-11-19 19:00
 */
@Component
@RequiredArgsConstructor
public class EasyRetryStartListener implements ApplicationRunner {
    private final List<Lifecycle> lifecycleList;
    private volatile boolean isStarted = false;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (isStarted) {
            EasyRetryLog.LOCAL.info("snail-job client already started v{}", EasyRetryVersion.getVersion());
            return;
        }

        System.out.println(MessageFormatter.format(SystemConstants.LOGO, EasyRetryVersion.getVersion()).getMessage());
        EasyRetryLog.LOCAL.info("snail-job client is preparing to start... v{}", EasyRetryVersion.getVersion());
        SpringContext.getContext().publishEvent(new SnailClientStartingEvent());
        lifecycleList.forEach(Lifecycle::start);
        SpringContext.getContext().publishEvent(new SnailClientStartedEvent());
        isStarted = true;
        EasyRetryLog.LOCAL.info("snail-job client started successfully v{}", EasyRetryVersion.getVersion());
    }

}
