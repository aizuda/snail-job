package com.x.retry.client.core.init;

import com.x.retry.client.core.Lifecycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 重试对账系统启动监听器
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-19 19:00
 */
@Component
@Slf4j
public class StartListener implements ApplicationRunner {

    @Autowired
    private List<Lifecycle> lifecycleList;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("X-RETRY-CLIENT-RETRY 启动");
        lifecycleList.forEach(Lifecycle::start);
        log.info("X-RETRY-CLIENT-RETRY 启动成功");
    }
}
