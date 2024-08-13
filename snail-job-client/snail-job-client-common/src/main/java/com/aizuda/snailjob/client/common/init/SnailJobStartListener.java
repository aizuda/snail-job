package com.aizuda.snailjob.client.common.init;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.event.SnailClientStartedEvent;
import com.aizuda.snailjob.client.common.event.SnailClientStartingEvent;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.util.SnailJobVersion;
import com.aizuda.snailjob.common.log.SnailJobLog;
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
public class SnailJobStartListener implements ApplicationRunner {
    private final List<Lifecycle> lifecycleList;
    private volatile boolean isStarted = false;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (isStarted) {
            SnailJobLog.LOCAL.info("snail-job client already started v{}", SnailJobVersion.getVersion());
            return;
        }

        System.out.println(MessageFormatter.format(SystemConstants.LOGO, SnailJobVersion.getVersion()).getMessage());
        SnailJobLog.LOCAL.info("snail-job client is preparing to start... v{}", SnailJobVersion.getVersion());
        SnailSpringContext.getContext().publishEvent(new SnailClientStartingEvent());
        lifecycleList.forEach(Lifecycle::start);
        SnailSpringContext.getContext().publishEvent(new SnailClientStartedEvent());
        isStarted = true;
        SnailJobLog.LOCAL.info("snail-job client started successfully v{}", SnailJobVersion.getVersion());
    }

}
