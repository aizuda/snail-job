package com.aizuda.easy.retry.client.core.init;

import com.aizuda.easy.retry.client.core.Lifecycle;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.util.EasyRetryVersion;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统启动监听器
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-19 19:00
 */
@Component
@Slf4j
public class EasyRetryStartListener implements ApplicationRunner {

    @Autowired
    private List<Lifecycle> lifecycleList;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println(MessageFormatter.format(SystemConstants.LOGO, EasyRetryVersion.getVersion()).getMessage());

        log.info("Easy-Retry client is preparing to start... v{}", EasyRetryVersion.getVersion());
        lifecycleList.forEach(Lifecycle::start);
        log.info("Easy-Retry client started successfully v{}", EasyRetryVersion.getVersion());
    }

}
