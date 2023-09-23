package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.scan;

import akka.actor.AbstractActor;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.retry.task.support.IdempotentStrategy;
import com.aizuda.easy.retry.server.common.dto.ScanTask;
import com.aizuda.easy.retry.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.task.TaskActuator;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.task.TaskActuatorSceneEnum;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 数据扫描模板类
 *
 * @author: www.byteblogs.com
 * @date : 2023-06-05 15:44
 * @since 1.5.0
 */
@Slf4j
public abstract class AbstractScanGroup extends AbstractActor {

    @Autowired
    @Qualifier("bitSetIdempotentStrategyHandler")
    protected IdempotentStrategy<String, Integer> idempotentStrategy;
    @Autowired
    protected SystemProperties systemProperties;
    @Autowired
    protected AccessTemplate accessTemplate;
    @Autowired
    protected ClientNodeAllocateHandler clientNodeAllocateHandler;
    @Autowired
    protected List<TaskActuator> taskActuators;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ScanTask.class, config -> {

            try {
                doScan(config);
            } catch (Exception e) {
                LogUtils.error(log, "Data scanner processing exception. [{}]", config, e);
            }

        }).build();

    }

    protected void doScan(final ScanTask scanTask) {

        LocalDateTime lastAt = LocalDateTime.now().minusDays(systemProperties.getLastDays());
        int retryPullPageSize = systemProperties.getRetryPullPageSize();
        String groupName = scanTask.getGroupName();
        Long lastId = Optional.ofNullable(getLastId(groupName)).orElse(0L);

        // 扫描当前Group 待处理的任务
        List<RetryTask> list = listAvailableTasks(groupName, lastAt, lastId, retryPullPageSize, taskActuatorScene().getScene());

        if (!CollectionUtils.isEmpty(list)) {

            // 更新拉取的最大的id
            putLastId(scanTask.getGroupName(), list.get(list.size() - 1).getId());

            for (RetryTask retryTask : list) {
                for (TaskActuator taskActuator : taskActuators) {
                    if (taskActuatorScene().getScene() == taskActuator.getTaskType().getScene()) {
                        taskActuator.actuator(retryTask);
                    }
                }
            }
        } else {
            // 数据为空则休眠5s
            try {
                Thread.sleep((10 / 2) * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            putLastId(groupName, 0L);
        }

    }

    protected abstract TaskActuatorSceneEnum taskActuatorScene();

    protected abstract Long getLastId(String groupName);

    protected abstract void putLastId(String groupName, Long lastId);

    public List<RetryTask> listAvailableTasks(String groupName,
                                              LocalDateTime lastAt,
                                              Long lastId,
                                              Integer pageSize,
                                              Integer taskType) {
        return accessTemplate.getRetryTaskAccess().listPage(groupName, new PageDTO<>(0, pageSize),
                        new LambdaQueryWrapper<RetryTask>()
                                .eq(RetryTask::getRetryStatus, RetryStatusEnum.RUNNING.getStatus())
                                .eq(RetryTask::getGroupName, groupName)
                                .eq(RetryTask::getTaskType, taskType)
                                .gt(RetryTask::getId, lastId)
                                .gt(RetryTask::getCreateDt, lastAt)
                                .orderByAsc(RetryTask::getId)
                                .orderByAsc(RetryTask::getCreateDt))
                .getRecords();
    }

}
