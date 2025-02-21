package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.constant.LogFieldConstants;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.RetryTaskLogMessageQueryVO;
import com.aizuda.snailjob.server.web.model.request.RetryTaskQueryVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.response.RetryTaskLogMessageResponseVO;
import com.aizuda.snailjob.server.web.model.response.RetryTaskLogResponseVO;
import com.aizuda.snailjob.server.web.service.RetryTaskLogService;
import com.aizuda.snailjob.server.web.service.convert.RetryTaskLogResponseVOConverter;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLogMessage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: opensnail
 * @date : 2022-02-28 09:10
 */
@Service
@RequiredArgsConstructor
public class RetryTaskLogServiceImpl implements RetryTaskLogService {

    private final RetryTaskMapper retryTaskMapper;
    private final RetryTaskLogMessageMapper retryTaskLogMessageMapper;

    @Override
    public PageResult<List<RetryTaskLogResponseVO>> getRetryTaskLogPage(RetryTaskQueryVO queryVO) {
        PageDTO<RetryTask> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        String namespaceId = userSessionVO.getNamespaceId();

        List<String> groupNames = UserSessionUtils.getGroupNames(queryVO.getGroupName());

        LambdaQueryWrapper<RetryTask> wrapper = new LambdaQueryWrapper<RetryTask>()
                .eq(RetryTask::getNamespaceId, namespaceId)
                .in(CollUtil.isNotEmpty(groupNames), RetryTask::getGroupName, groupNames)
                .eq(StrUtil.isNotBlank(queryVO.getSceneName()), RetryTask::getSceneName, queryVO.getSceneName())
                .eq(queryVO.getRetryStatus() != null, RetryTask::getTaskStatus, queryVO.getRetryStatus())
                .eq(Objects.nonNull(queryVO.getRetryId()), RetryTask::getRetryId, queryVO.getRetryId())
                .between(ObjUtil.isNotNull(queryVO.getDatetimeRange()),
                        RetryTask::getCreateDt, queryVO.getStartDt(), queryVO.getEndDt())
                .select(RetryTask::getGroupName, RetryTask::getId, RetryTask::getSceneName, RetryTask::getTaskStatus,
                        RetryTask::getCreateDt, RetryTask::getTaskType, RetryTask::getOperationReason, RetryTask::getRetryId)
                .orderByDesc(RetryTask::getCreateDt);

        PageDTO<RetryTask> retryTaskPageDTO = retryTaskMapper.selectPage(pageDTO, wrapper);
        return new PageResult<>(retryTaskPageDTO,
                RetryTaskLogResponseVOConverter.INSTANCE.convertList(retryTaskPageDTO.getRecords()));

    }

    @Override
    public RetryTaskLogMessageResponseVO getRetryTaskLogMessagePage(
            RetryTaskLogMessageQueryVO queryVO) {
        if (queryVO.getRetryTaskId() == null || StrUtil.isBlank(queryVO.getGroupName())) {
            RetryTaskLogMessageResponseVO jobLogResponseVO = new RetryTaskLogMessageResponseVO();
            jobLogResponseVO.setNextStartId(0L);
            jobLogResponseVO.setFromIndex(0);
            return jobLogResponseVO;
        }

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        PageDTO<RetryTaskLogMessage> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        PageDTO<RetryTaskLogMessage> selectPage = retryTaskLogMessageMapper.selectPage(pageDTO,
                new LambdaQueryWrapper<RetryTaskLogMessage>()
                        .select(RetryTaskLogMessage::getId, RetryTaskLogMessage::getLogNum)
                        .ge(RetryTaskLogMessage::getId, queryVO.getStartId())
                        .eq(RetryTaskLogMessage::getNamespaceId, namespaceId)
                        .eq(RetryTaskLogMessage::getRetryTaskId, queryVO.getRetryTaskId())
                        .eq(RetryTaskLogMessage::getGroupName, queryVO.getGroupName())
                        .orderByAsc(RetryTaskLogMessage::getId).orderByAsc(RetryTaskLogMessage::getRealTime)
                        .orderByDesc(RetryTaskLogMessage::getCreateDt));

        List<RetryTaskLogMessage> records = selectPage.getRecords();

        if (CollUtil.isEmpty(records)) {
            return new RetryTaskLogMessageResponseVO()
                    .setFinished(Boolean.TRUE)
                    .setNextStartId(queryVO.getStartId())
                    .setFromIndex(0);
        }

        Integer fromIndex = Optional.ofNullable(queryVO.getFromIndex()).orElse(0);
        RetryTaskLogMessage firstRecord = records.get(0);
        List<Long> ids = Lists.newArrayList(firstRecord.getId());
        int total = firstRecord.getLogNum() - fromIndex;
        for (int i = 1; i < records.size(); i++) {
            RetryTaskLogMessage record = records.get(i);
            if (total + record.getLogNum() > queryVO.getSize()) {
                break;
            }

            total += record.getLogNum();
            ids.add(record.getId());
        }

        long nextStartId = 0;
        List<Map<String, String>> messages = Lists.newArrayList();
        List<RetryTaskLogMessage> jobLogMessages = retryTaskLogMessageMapper.selectList(
                new LambdaQueryWrapper<RetryTaskLogMessage>()
                        .in(RetryTaskLogMessage::getId, ids)
                        .orderByAsc(RetryTaskLogMessage::getId)
                        .orderByAsc(RetryTaskLogMessage::getRealTime)
        );

        for (final RetryTaskLogMessage retryTaskLogMessage : jobLogMessages) {

            List<Map<String, String>> originalList = JsonUtil.parseObject(retryTaskLogMessage.getMessage(), List.class);
            int size = originalList.size() - fromIndex;
            List<Map<String, String>> pageList = originalList.stream().skip(fromIndex).limit(queryVO.getSize())
                    .collect(Collectors.toList());
            if (messages.size() + size >= queryVO.getSize()) {
                messages.addAll(pageList);
                nextStartId = retryTaskLogMessage.getId();
                fromIndex = Math.min(fromIndex + queryVO.getSize(), originalList.size() - 1) + 1;
                break;
            }

            messages.addAll(pageList);
            nextStartId = retryTaskLogMessage.getId() + 1;
            fromIndex = 0;
        }

        messages = messages.stream()
                .sorted(Comparator.comparingLong(o -> Long.parseLong(o.get(LogFieldConstants.TIME_STAMP))))
                .collect(Collectors.toList());

        RetryTaskLogMessageResponseVO responseVO = new RetryTaskLogMessageResponseVO();
        responseVO.setMessage(messages);
        responseVO.setNextStartId(nextStartId);
        responseVO.setFromIndex(fromIndex);

        return responseVO;
    }

    @Override
    public RetryTaskLogResponseVO getRetryTaskLogById(Long id) {
        RetryTask retryTask = retryTaskMapper.selectById(id);
        return RetryTaskLogResponseVOConverter.INSTANCE.convert(retryTask);
    }

    @Override
    @Transactional
    public boolean deleteById(final Long id) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        RetryTask retryTask = retryTaskMapper.selectOne(
                new LambdaQueryWrapper<RetryTask>()
                        .in(RetryTask::getTaskStatus, List.of(RetryStatusEnum.FINISH.getStatus(), RetryStatusEnum.MAX_COUNT.getStatus()))
                        .eq(RetryTask::getNamespaceId, namespaceId)
                        .eq(RetryTask::getId, id));
        Assert.notNull(retryTask, () -> new SnailJobServerException("数据删除失败"));

        retryTaskLogMessageMapper.delete(new LambdaQueryWrapper<RetryTaskLogMessage>()
                .eq(RetryTaskLogMessage::getNamespaceId, namespaceId)
                .eq(RetryTaskLogMessage::getGroupName, retryTask.getGroupName())
                .eq(RetryTaskLogMessage::getRetryTaskId, id)
        );

        return 1 == retryTaskMapper.deleteById(id);
    }

    @Override
    @Transactional
    public boolean batchDelete(final Set<Long> ids) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        List<RetryTask> retryTasks = retryTaskMapper.selectList(
                new LambdaQueryWrapper<RetryTask>()
                        .in(RetryTask::getTaskStatus, List.of(RetryStatusEnum.FINISH.getStatus(), RetryStatusEnum.MAX_COUNT.getStatus()))
                        .eq(RetryTask::getNamespaceId, namespaceId)
                        .in(RetryTask::getId, ids));
        Assert.notEmpty(retryTasks, () -> new SnailJobServerException("数据不存在"));
        Assert.isTrue(retryTasks.size() == ids.size(), () -> new SnailJobServerException("数据不存在"));

        for (final RetryTask retryTask : retryTasks) {
            retryTaskLogMessageMapper.delete(
                    new LambdaQueryWrapper<RetryTaskLogMessage>()
                            .eq(RetryTaskLogMessage::getNamespaceId, namespaceId)
                            .eq(RetryTaskLogMessage::getGroupName, retryTask.getGroupName())
                            .eq(RetryTaskLogMessage::getRetryTaskId, retryTask.getId()));
        }
        return 1 == retryTaskMapper.deleteByIds(ids);
    }
}
