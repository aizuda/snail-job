package com.aizuda.easy.retry.server.retry.task.support.schedule;

import cn.hutool.core.lang.Pair;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.schedule.AbstractSchedule;
import com.aizuda.easy.retry.server.retry.task.service.RetryService;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.po.GroupConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

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
public class RetryTaskSchedule extends AbstractSchedule implements Lifecycle {

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
            Set<Pair<String/*groupName*/, String/*namespaceId*/>> groupNameList = accessTemplate.getGroupConfigAccess()
                .list(new LambdaQueryWrapper<GroupConfig>()
                    .select(GroupConfig::getGroupName, GroupConfig::getNamespaceId)
                        .eq(GroupConfig::getGroupStatus, StatusEnum.YES.getStatus()))
                .stream().map(groupConfig -> {


                    return Pair.of(groupConfig.getGroupName(), groupConfig.getNamespaceId());
                    }).collect(Collectors.toSet());

            for (Pair<String/*groupName*/, String/*namespaceId*/> pair : groupNameList) {
                retryService.moveDeadLetterAndDelFinish(pair.getKey(), pair.getValue());
            }

        } catch (Exception e) {
            EasyRetryLog.LOCAL.error("clearFinishAndMoveDeadLetterRetryTask 失败", e);
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
