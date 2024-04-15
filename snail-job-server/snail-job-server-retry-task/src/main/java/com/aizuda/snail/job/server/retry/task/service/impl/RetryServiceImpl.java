package com.aizuda.snail.job.server.retry.task.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snail.job.common.core.enums.RetryStatusEnum;
import com.aizuda.snail.job.common.core.util.JsonUtil;
import com.aizuda.snail.job.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snail.job.server.common.exception.EasyRetryServerException;
import com.aizuda.snail.job.server.retry.task.service.RetryDeadLetterConverter;
import com.aizuda.snail.job.server.retry.task.service.RetryService;
import com.aizuda.snail.job.server.retry.task.support.event.RetryTaskFailDeadLetterAlarmEvent;
import com.aizuda.snail.job.template.datasource.access.AccessTemplate;
import com.aizuda.snail.job.template.datasource.access.TaskAccess;
import com.aizuda.snail.job.template.datasource.persistence.po.RetryDeadLetter;
import com.aizuda.snail.job.template.datasource.persistence.po.RetryTask;
import com.aizuda.snail.job.template.datasource.utils.RequestDataHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 重试服务层实现
 *
 * @author: opensnail
 * @date : 2021-11-26 15:19
 */
@Service
@Slf4j
public class RetryServiceImpl implements RetryService {

    @Autowired
    private AccessTemplate accessTemplate;

    @Autowired
    private ApplicationContext context;

    @Transactional
    @Override
    public Boolean moveDeadLetterAndDelFinish(String groupName, String namespaceId) {

        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        RequestDataHelper.setPartition(groupName, namespaceId);
        List<RetryTask> callbackRetryTasks = retryTaskAccess.listPage(groupName, namespaceId, new PageDTO<>(0, 100),
                new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getNamespaceId, namespaceId)
                        .in(RetryTask::getRetryStatus, RetryStatusEnum.MAX_COUNT.getStatus(),
                                RetryStatusEnum.FINISH.getStatus())
                        .eq(RetryTask::getTaskType, SyetemTaskTypeEnum.CALLBACK.getType())
                        .eq(RetryTask::getGroupName, groupName)).getRecords();

        if (CollectionUtils.isEmpty(callbackRetryTasks)) {
            return Boolean.TRUE;
        }

        Set<String> uniqueIdSet = callbackRetryTasks.stream().map(callbackTask -> {
            String callbackTaskUniqueId = callbackTask.getUniqueId();
            return callbackTaskUniqueId.substring(callbackTaskUniqueId.lastIndexOf(StrUtil.UNDERLINE) + 1);
        }).collect(Collectors.toSet());

        List<RetryTask> retryTasks = accessTemplate.getRetryTaskAccess()
                .list(groupName, namespaceId, new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getNamespaceId, namespaceId)
                        .eq(RetryTask::getTaskType, SyetemTaskTypeEnum.RETRY.getType())
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

        moveDeadLetters(groupName, namespaceId, waitMoveDeadLetters);

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
                        .delete(groupName, namespaceId, new LambdaQueryWrapper<RetryTask>()
                                .eq(RetryTask::getNamespaceId, namespaceId)
                                .eq(RetryTask::getGroupName, groupName)
                                .in(RetryTask::getId, waitDelRetryFinishSet)),
                () -> new EasyRetryServerException("删除重试数据失败 [{}]", JsonUtil.toJsonString(retryTasks)));
        return Boolean.TRUE;
    }

    /**
     * 迁移死信队列数据
     *
     * @param groupName  组id
     * @param retryTasks 待迁移数据
     */
    private void moveDeadLetters(String groupName, String namespaceId, List<RetryTask> retryTasks) {
        if (CollectionUtils.isEmpty(retryTasks)) {
            return;
        }

        List<RetryDeadLetter> retryDeadLetters = RetryDeadLetterConverter.INSTANCE.toRetryDeadLetter(retryTasks);
        LocalDateTime now = LocalDateTime.now();
        for (RetryDeadLetter retryDeadLetter : retryDeadLetters) {
            retryDeadLetter.setCreateDt(now);
        }
        Assert.isTrue(retryDeadLetters.size() == accessTemplate
                        .getRetryDeadLetterAccess().batchInsert(groupName, namespaceId, retryDeadLetters),
                () -> new EasyRetryServerException("插入死信队列失败 [{}]", JsonUtil.toJsonString(retryDeadLetters)));

        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        List<Long> ids = retryTasks.stream().map(RetryTask::getId).collect(Collectors.toList());
        Assert.isTrue(retryTasks.size() == retryTaskAccess.delete(groupName, namespaceId,
                        new LambdaQueryWrapper<RetryTask>()
                                .eq(RetryTask::getNamespaceId, namespaceId)
                                .eq(RetryTask::getGroupName, groupName)
                                .in(RetryTask::getId, ids)),
                () -> new EasyRetryServerException("删除重试数据失败 [{}]", JsonUtil.toJsonString(retryTasks)));

        context.publishEvent(new RetryTaskFailDeadLetterAlarmEvent(retryDeadLetters));
    }

}
