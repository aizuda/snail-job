package com.aizuda.easy.retry.server.retry.task.support.schedule;

import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.retry.task.service.RetryService;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * 删除重试完成的和重试到达最大重试次数的数据迁移到死信队列表
 *
 * @author: www.byteblogs.com
 * @date : 2023-07-21 17:19
 * @since 2.1.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RetryTaskSchedule extends AbstractSchedule implements Lifecycle  {
    private final RetryService retryService;
    @Autowired
    protected AccessTemplate accessTemplate;
    @Override
    public void start() {
        taskScheduler.scheduleWithFixedDelay(this::execute, Instant.now(), Duration.parse("PT1H"));
    }

    @Override
    public void close() {

    }

    @Override
    protected void doExecute() {
        try {
            Set<String> groupNameList = accessTemplate.getGroupConfigAccess().getGroupNameList();

            for (String groupName : groupNameList) {
                retryService.moveDeadLetterAndDelFinish(groupName);
            }

        } catch (Exception e) {
            LogUtils.error(log, "clearFinishAndMoveDeadLetterRetryTask 失败", e);
        }
    }

    @Override
    public String lockName() {
        return "clearFinishAndMoveDeadLetterRetryTask";
    }

    @Override
    public String lockAtMost() {
        return "PT60s";
    }

    @Override
    public String lockAtLeast() {
        return "PT60s";
    }
}
