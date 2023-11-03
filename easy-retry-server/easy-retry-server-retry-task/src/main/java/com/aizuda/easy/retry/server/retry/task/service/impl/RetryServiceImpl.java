package com.aizuda.easy.retry.server.retry.task.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.enums.DelayLevelEnum;
import com.aizuda.easy.retry.server.common.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.generator.id.IdGenerator;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import com.aizuda.easy.retry.server.retry.task.service.RetryDeadLetterConverter;
import com.aizuda.easy.retry.server.retry.task.service.RetryService;
import com.aizuda.easy.retry.server.retry.task.support.RetryTaskConverter;
import com.aizuda.easy.retry.server.retry.task.support.RetryTaskLogConverter;
import com.aizuda.easy.retry.server.common.strategy.WaitStrategies;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.access.ConfigAccess;
import com.aizuda.easy.retry.template.datasource.access.TaskAccess;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.*;
import com.aizuda.easy.retry.template.datasource.utils.RequestDataHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 重试服务层实现
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-26 15:19
 */
@Service
@Slf4j
public class RetryServiceImpl implements RetryService {

    @Autowired
    private AccessTemplate accessTemplate;

    @Transactional
    @Override
    public Boolean moveDeadLetterAndDelFinish(String groupName) {

        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        RequestDataHelper.setPartition(groupName);
        List<RetryTask> callbackRetryTasks = retryTaskAccess.listPage(groupName, new PageDTO<>(0, 100),
            new LambdaQueryWrapper<RetryTask>()
                .in(RetryTask::getRetryStatus, RetryStatusEnum.MAX_COUNT.getStatus(),
                    RetryStatusEnum.FINISH.getStatus())
                .eq(RetryTask::getTaskType, TaskTypeEnum.CALLBACK.getType())
                .eq(RetryTask::getGroupName, groupName)).getRecords();

        if (CollectionUtils.isEmpty(callbackRetryTasks)) {
            return Boolean.TRUE;
        }

        Set<String> uniqueIdSet = callbackRetryTasks.stream().map(callbackTask -> {
            String callbackTaskUniqueId = callbackTask.getUniqueId();
            return callbackTaskUniqueId.substring(callbackTaskUniqueId.lastIndexOf(StrUtil.UNDERLINE) + 1);
        }).collect(Collectors.toSet());

        List<RetryTask> retryTasks = accessTemplate.getRetryTaskAccess().list(groupName, new LambdaQueryWrapper<RetryTask>()
            .eq(RetryTask::getTaskType, TaskTypeEnum.RETRY.getType())
            .in(RetryTask::getUniqueId, uniqueIdSet)
        );

        // 迁移重试失败的数据
        List<RetryTask> waitMoveDeadLetters = new ArrayList<>();
        List<RetryTask> maxCountRetryTaskList = retryTasks.stream()
            .filter(retryTask -> retryTask.getRetryStatus().equals(RetryStatusEnum.MAX_COUNT.getStatus())).collect(
                Collectors.toList());
        if (!CollectionUtils.isEmpty(maxCountRetryTaskList)) {
            waitMoveDeadLetters.addAll(maxCountRetryTaskList);
        }

        List<RetryTask> maxCountCallbackRetryTaskList = callbackRetryTasks.stream()
            .filter(retryTask -> retryTask.getRetryStatus().equals(RetryStatusEnum.MAX_COUNT.getStatus())).collect(
                Collectors.toList());

        if (!CollectionUtils.isEmpty(maxCountRetryTaskList)) {
            waitMoveDeadLetters.addAll(maxCountCallbackRetryTaskList);
        }

        moveDeadLetters(groupName, waitMoveDeadLetters);

        // 删除重试完成的数据
        Set<Long> waitDelRetryFinishSet = new HashSet<>();
        Set<Long> finishRetryIdList = retryTasks.stream()
            .filter(retryTask -> retryTask.getRetryStatus().equals(RetryStatusEnum.FINISH.getStatus()))
            .map(RetryTask::getId)
            .collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(finishRetryIdList)) {
            waitDelRetryFinishSet.addAll(finishRetryIdList);
        }

        Set<Long> finishCallbackRetryIdList = callbackRetryTasks.stream()
            .filter(retryTask -> retryTask.getRetryStatus().equals(RetryStatusEnum.FINISH.getStatus()))
            .map(RetryTask::getId)
            .collect(Collectors.toSet());

        // 迁移重试失败的数据
        if (!CollectionUtils.isEmpty(finishCallbackRetryIdList)) {
            waitDelRetryFinishSet.addAll(finishCallbackRetryIdList);
        }

        if (CollectionUtils.isEmpty(waitDelRetryFinishSet)) {
            return Boolean.TRUE;
        }

        Assert.isTrue(waitDelRetryFinishSet.size() == accessTemplate.getRetryTaskAccess()
                        .delete(groupName, new LambdaQueryWrapper<RetryTask>()
                                .eq(RetryTask::getGroupName, groupName).in(RetryTask::getId, waitDelRetryFinishSet)),
            () -> new EasyRetryServerException("删除重试数据失败 [{}]", JsonUtil.toJsonString(retryTasks)));

        return Boolean.TRUE;
    }

    /**
     * 迁移死信队列数据
     *
     * @param groupName  组id
     * @param retryTasks 待迁移数据
     */
    private void moveDeadLetters(String groupName, List<RetryTask> retryTasks) {
        if (CollectionUtils.isEmpty(retryTasks)) {
            return;
        }

        List<RetryDeadLetter> retryDeadLetters = RetryDeadLetterConverter.INSTANCE.toRetryDeadLetter(retryTasks);
        LocalDateTime now = LocalDateTime.now();
        for (RetryDeadLetter retryDeadLetter : retryDeadLetters) {
            retryDeadLetter.setCreateDt(now);
        }
        Assert.isTrue(retryDeadLetters.size() == accessTemplate
                        .getRetryDeadLetterAccess().batchInsert(groupName, retryDeadLetters),
            () -> new EasyRetryServerException("插入死信队列失败 [{}]", JsonUtil.toJsonString(retryDeadLetters)));

        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        List<Long> ids = retryTasks.stream().map(RetryTask::getId).collect(Collectors.toList());
        Assert.isTrue(retryTasks.size() == retryTaskAccess.delete(groupName, new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getGroupName, groupName).in(RetryTask::getId, ids)),
            () -> new EasyRetryServerException("删除重试数据失败 [{}]", JsonUtil.toJsonString(retryTasks)));
    }

}
