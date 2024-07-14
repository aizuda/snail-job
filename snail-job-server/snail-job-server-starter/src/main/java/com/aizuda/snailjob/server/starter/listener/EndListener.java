package com.aizuda.snailjob.server.starter.listener;

import com.aizuda.snailjob.common.core.util.SnailJobVersion;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 关闭监听器
 *
 * @author: opensnail
 * @date : 2021-11-19 19:00
 */
@Component
public class EndListener implements ApplicationListener<ContextClosedEvent> {

    @Autowired
    private List<Lifecycle> lifecycleList;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        SnailJobLog.LOCAL.info("snail-job client about to shutdown v{}", SnailJobVersion.getVersion());
        lifecycleList.forEach(Lifecycle::close);
        SnailJobLog.LOCAL.info("snail-job client closed successfully v{}", SnailJobVersion.getVersion());
    }
}
