package com.aizuda.easy.retry.server.support.schedule;

import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import com.aizuda.easy.retry.server.service.RetryService;
import com.aizuda.easy.retry.server.support.handler.ServerRegisterNodeHandler;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 清除数据线程调度器
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-22 11:00
 */
@Component
@Slf4j
public class ClearThreadSchedule {

    @Autowired
    private ServerNodeMapper serverNodeMapper;

    @Autowired
    private RetryService retryService;

    @Autowired
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;

    /**
     * 删除过期下线机器
     */
    @Scheduled(fixedRate = 5000)
    @SchedulerLock(name = "clearOfflineNode", lockAtMostFor = "PT10s", lockAtLeastFor = "PT5s")
    public void clearOfflineNode() {

        try {
            serverNodeMapper.deleteByExpireAt(LocalDateTime.now().minusSeconds(ServerRegisterNodeHandler.DELAY_TIME * 2));
        } catch (Exception e) {
            LogUtils.error(log, "clearOfflineNode 失败", e);
        }

    }

    /**
     * 删除重试完成的和重试到达最大重试次数的数据迁移到死信队列表
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    @SchedulerLock(name = "clearFinishAndMoveDeadLetterRetryTask", lockAtMostFor = "PT60s", lockAtLeastFor = "PT60s")
    public void clearFinishAndMoveDeadLetterRetryTask() {

        try {
            Set<String> groupNameList = configAccess.getGroupNameList();

            for (String groupName : groupNameList) {
                retryService.moveDeadLetterAndDelFinish(groupName);
            }

        } catch (Exception e) {
            LogUtils.error(log, "clearFinishAndMoveDeadLetterRetryTask 失败", e);
        }

    }

}
