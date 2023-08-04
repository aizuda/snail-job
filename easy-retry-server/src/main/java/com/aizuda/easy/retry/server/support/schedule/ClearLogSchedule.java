package com.aizuda.easy.retry.server.support.schedule;

import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.config.SystemProperties;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLogMessage;
import com.aizuda.easy.retry.server.support.Lifecycle;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 清理日志 一小时运行一次
 *
 * @author: www.byteblogs.com
 * @date : 2023-07-21 13:32
 * @since 2.1.0
 */
@Component
@Slf4j
public class ClearLogSchedule extends AbstractSchedule implements Lifecycle {

    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;
    @Autowired
    private SystemProperties systemProperties;
    @Autowired
    private RetryTaskLogMessageMapper retryTaskLogMessageMapper;

    @Override
    public String lockName() {
        return "clearLog";
    }

    @Override
    public String lockAtMost() {
        return "PT1H";
    }

    @Override
    public String lockAtLeast() {
        return "PT1M";
    }

    @Override
    protected void doExecute() {
        try {
            LocalDateTime endTime = LocalDateTime.now().minusDays(systemProperties.getLogStorage());
            retryTaskLogMapper.delete(new LambdaUpdateWrapper<RetryTaskLog>().le(RetryTaskLog::getCreateDt, endTime));
            retryTaskLogMessageMapper.delete(new LambdaUpdateWrapper<RetryTaskLogMessage>().le(RetryTaskLogMessage::getCreateDt, endTime));
        } catch (Exception e) {
            LogUtils.error(log, "clear log error", e);
        }
    }

    @Override
    public void start() {
        taskScheduler.scheduleAtFixedRate(this::execute, Duration.parse("PT1H"));
    }

    @Override
    public void close() {

    }
}
