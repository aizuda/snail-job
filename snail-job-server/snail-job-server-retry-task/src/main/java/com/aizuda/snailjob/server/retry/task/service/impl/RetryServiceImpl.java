package com.aizuda.snailjob.server.retry.task.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.retry.task.service.RetryDeadLetterConverter;
import com.aizuda.snailjob.server.retry.task.service.RetryService;
import com.aizuda.snailjob.server.retry.task.support.event.RetryTaskFailDeadLetterAlarmEvent;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.TaskAccess;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryDeadLetter;
import com.aizuda.snailjob.template.datasource.utils.RequestDataHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 重试服务层实现
 * todo 重新设计
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

        TaskAccess<Retry> retryTaskAccess = accessTemplate.getRetryAccess();
        RequestDataHelper.setPartition(groupName, namespaceId);
        List<Retry> callbackRetries = retryTaskAccess.listPage(new PageDTO<>(0, 100),
                new LambdaQueryWrapper<Retry>()
                        .eq(Retry::getNamespaceId, namespaceId)
                        .in(Retry::getRetryStatus, RetryStatusEnum.MAX_COUNT.getStatus(),
                                RetryStatusEnum.FINISH.getStatus())
                        .eq(Retry::getTaskType, SyetemTaskTypeEnum.CALLBACK.getType())
                        .eq(Retry::getGroupName, groupName)
                        .orderByDesc(Retry::getId)).getRecords();

        if (CollUtil.isEmpty(callbackRetries)) {
            return Boolean.TRUE;
        }

        Set<Long> uniqueIdSet = StreamUtils.toSet(callbackRetries, Retry::getId);

        List<Retry> retries = accessTemplate.getRetryAccess()
                .list(new LambdaQueryWrapper<Retry>()
                        .eq(Retry::getNamespaceId, namespaceId)
                        .eq(Retry::getTaskType, SyetemTaskTypeEnum.RETRY.getType())
//                        .in(Retry::getUniqueId, uniqueIdSet)
                );

        // 迁移重试失败的数据
        List<Retry> waitMoveDeadLetters = new ArrayList<>();
        List<Retry> maxCountRetryList = retries.stream()
                .filter(retryTask -> retryTask.getRetryStatus().equals(RetryStatusEnum.MAX_COUNT.getStatus())).collect(
                        Collectors.toList());
        if (CollUtil.isNotEmpty(maxCountRetryList)) {
            waitMoveDeadLetters.addAll(maxCountRetryList);
        }

        List<Retry> maxCountCallbackRetryList = callbackRetries.stream()
                .filter(retryTask -> retryTask.getRetryStatus().equals(RetryStatusEnum.MAX_COUNT.getStatus())).collect(
                        Collectors.toList());

        if (CollUtil.isNotEmpty(maxCountRetryList)) {
            waitMoveDeadLetters.addAll(maxCountCallbackRetryList);
        }

        moveDeadLetters(groupName, namespaceId, waitMoveDeadLetters);

        // 删除重试完成的数据
        Set<Long> waitDelRetryFinishSet = new HashSet<>();
        Set<Long> finishRetryIdList = retries.stream()
                .filter(retryTask -> retryTask.getRetryStatus().equals(RetryStatusEnum.FINISH.getStatus()))
                .map(Retry::getId)
                .collect(Collectors.toSet());
        if (CollUtil.isNotEmpty(finishRetryIdList)) {
            waitDelRetryFinishSet.addAll(finishRetryIdList);
        }

        Set<Long> finishCallbackRetryIdList = callbackRetries.stream()
                .filter(retryTask -> retryTask.getRetryStatus().equals(RetryStatusEnum.FINISH.getStatus()))
                .map(Retry::getId)
                .collect(Collectors.toSet());

        // 迁移重试失败的数据
        if (CollUtil.isNotEmpty(finishCallbackRetryIdList)) {
            waitDelRetryFinishSet.addAll(finishCallbackRetryIdList);
        }

        if (CollUtil.isEmpty(waitDelRetryFinishSet)) {
            return Boolean.TRUE;
        }

        Assert.isTrue(waitDelRetryFinishSet.size() == accessTemplate.getRetryAccess()
                        .delete(new LambdaQueryWrapper<Retry>()
                                .eq(Retry::getNamespaceId, namespaceId)
                                .eq(Retry::getGroupName, groupName)
                                .in(Retry::getId, waitDelRetryFinishSet)),
                () -> new SnailJobServerException("删除重试数据失败 [{}]", JsonUtil.toJsonString(retries)));
        return Boolean.TRUE;
    }

    /**
     * 迁移死信队列数据
     *
     * @param groupName  组id
     * @param retries 待迁移数据
     */
    private void moveDeadLetters(String groupName, String namespaceId, List<Retry> retries) {
        if (CollUtil.isEmpty(retries)) {
            return;
        }

        List<RetryDeadLetter> retryDeadLetters = RetryDeadLetterConverter.INSTANCE.toRetryDeadLetter(retries);
        LocalDateTime now = LocalDateTime.now();
        for (RetryDeadLetter retryDeadLetter : retryDeadLetters) {
            retryDeadLetter.setCreateDt(now);
        }
        Assert.isTrue(retryDeadLetters.size() == accessTemplate
                        .getRetryDeadLetterAccess().insertBatch(retryDeadLetters),
                () -> new SnailJobServerException("插入死信队列失败 [{}]", JsonUtil.toJsonString(retryDeadLetters)));

        TaskAccess<Retry> retryTaskAccess = accessTemplate.getRetryAccess();
        Assert.isTrue(retries.size() == retryTaskAccess.delete(new LambdaQueryWrapper<Retry>()
                                .eq(Retry::getNamespaceId, namespaceId)
                                .eq(Retry::getGroupName, groupName)
                                .in(Retry::getId, StreamUtils.toList(retries, Retry::getId))),
                () -> new SnailJobServerException("删除重试数据失败 [{}]", JsonUtil.toJsonString(retries)));

        context.publishEvent(new RetryTaskFailDeadLetterAlarmEvent(retryDeadLetters));
    }

}
