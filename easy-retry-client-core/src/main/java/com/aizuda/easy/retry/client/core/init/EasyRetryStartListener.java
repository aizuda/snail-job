package com.aizuda.easy.retry.client.core.init;

import com.aizuda.easy.retry.client.core.Lifecycle;
import lombok.extern.slf4j.Slf4j;
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
        log.info("Easy-Retry client is preparing to start");
        lifecycleList.forEach(Lifecycle::start);
        log.info("Easy-Retry client started successfully");
    }
}