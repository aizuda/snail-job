package com.aizuda.easy.retry.server.support.schedule;

import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.config.SystemProperties;
import com.aizuda.easy.retry.server.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskLogMessageMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTaskLog;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTaskLogMessage;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import com.aizuda.easy.retry.server.service.RetryService;
import com.aizuda.easy.retry.server.support.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.support.register.ServerRegister;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 清除数据线程调度器
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-22 11:00
 */
@Component
@Slf4j
@Deprecated
public class ClearThreadSchedule {

    @Autowired
    private ServerNodeMapper serverNodeMapper;

    @Autowired
    private RetryService retryService;

    @Autowired
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;

    @Autowired
    private RetryTaskLogMessageMapper retryTaskLogMessageMapper;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;
    @Autowired
    private SystemProperties systemProperties;

    /**
     * 删除过期下线机器
     */
//    @Scheduled(fixedRate = 5000)
//    @SchedulerLock(name = "clearOfflineNode", lockAtMostFor = "PT10s", lockAtLeastFor = "PT5s")
    public void clearOfflineNode() {

        try {
            // 删除内存缓存的待下线的机器
            LocalDateTime endTime = LocalDateTime.now().minusSeconds(ServerRegister.DELAY_TIME + (ServerRegister.DELAY_TIME / 3));

            // 先删除DB中需要下线的机器
            serverNodeMapper.deleteByExpireAt(endTime);

            Set<RegisterNodeInfo> allPods = CacheRegisterTable.getAllPods();
            Set<RegisterNodeInfo> waitOffline = allPods.stream().filter(registerNodeInfo -> registerNodeInfo.getExpireAt().isBefore(endTime)).collect(Collectors.toSet());
            Set<String> podIds = waitOffline.stream().map(RegisterNodeInfo::getHostId).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(podIds)) {
                return;
            }

            for (final RegisterNodeInfo registerNodeInfo : waitOffline) {
                CacheRegisterTable.remove(registerNodeInfo.getGroupName(), registerNodeInfo.getHostId());
            }

        } catch (Exception e) {
            LogUtils.error(log, "clearOfflineNode 失败", e);
        }

    }

    /**
     * 删除重试完成的和重试到达最大重试次数的数据迁移到死信队列表
     */
//    @Scheduled(cron = "0 0 0/1 * * ?")
//    @SchedulerLock(name = "clearFinishAndMoveDeadLetterRetryTask", lockAtMostFor = "PT60s", lockAtLeastFor = "PT60s")
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

    /**
     * 清理日志 一小时运行一次
     */
//    @Scheduled(cron = "0 0 0/1 * * ? ")
//    @SchedulerLock(name = "clearLog", lockAtMostFor = "PT1H", lockAtLeastFor = "PT1H")
    public void clearLog() {
        try {
            LocalDateTime endTime = LocalDateTime.now().minusDays(systemProperties.getLogStorage());
            retryTaskLogMapper.delete(new LambdaUpdateWrapper<RetryTaskLog>().le(RetryTaskLog::getCreateDt, endTime));
            retryTaskLogMessageMapper.delete(new LambdaUpdateWrapper<RetryTaskLogMessage>().le(RetryTaskLogMessage::getCreateDt, endTime));
        } catch (Exception e) {
            LogUtils.error(log, "clear log error", e);
        }
    }

}
