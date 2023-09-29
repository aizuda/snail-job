package com.aizuda.easy.retry.server.job;

import com.aizuda.easy.retry.server.common.Lifecycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author www.byteblogs.com
 * @date 2023-09-29 23:29:44
 * @since 2.4.0
 */
@Component
@Slf4j
public class EasyRetryJobTaskStarter implements Lifecycle {

    @Override
    public void start() {
        // 检查是否还有未执行的任务，如果有则直接失败
        log.info("easy-retry-job-task starting...");

        log.info("easy-retry-job-task completed");
    }

    @Override
    public void close() {
        // 关闭还未执行的任务
    }
}
