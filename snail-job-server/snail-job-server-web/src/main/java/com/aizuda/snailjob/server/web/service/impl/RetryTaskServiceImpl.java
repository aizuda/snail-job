package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.RetryOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.constant.LogFieldConstants;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.retry.task.dto.TaskStopJobDTO;
import com.aizuda.snailjob.server.retry.task.support.handler.RetryTaskStopHandler;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.RetryTaskLogMessageQueryVO;
import com.aizuda.snailjob.server.web.model.request.RetryTaskQueryVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.response.RetryTaskLogMessageResponseVO;
import com.aizuda.snailjob.server.web.model.response.RetryTaskResponseVO;
import com.aizuda.snailjob.server.web.service.RetryTaskService;
import com.aizuda.snailjob.server.retry.task.convert.RetryConverter;
import com.aizuda.snailjob.server.web.service.convert.RetryTaskLogResponseVOConverter;
import com.aizuda.snailjob.server.web.service.convert.RetryTaskResponseVOConverter;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryMapper;
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
public class RetryTaskServiceImpl implements RetryTaskService {

    private final RetryTaskMapper retryTaskMapper;
    private final RetryMapper retryMapper;
    private final RetryTaskLogMessageMapper retryTaskLogMessageMapper;
    private final RetryTaskStopHandler retryTaskStopHandler;

    @Override
    public PageResult<List<RetryTaskResponseVO>> getRetryTaskLogPage(RetryTaskQueryVO queryVO) {
        PageDTO<RetryTask> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        String namespaceId = userSessionVO.getNamespaceId();

        List<String> groupNames = UserSessionUtils.getGroupNames(queryVO.getGroupName());

        LambdaQueryWrapper<RetryTask> wrapper = new LambdaQueryWrapper<RetryTask>()
                .eq(RetryTask::getNamespaceId, namespaceId)
                .in(CollUtil.isNotEmpty(groupNames), RetryTask::getGroupName, groupNames)
                .eq(StrUtil.isNotBlank(queryVO.getSceneName()), RetryTask::getSceneName, queryVO.getSceneName())
                .eq(queryVO.getTaskStatus() != null, RetryTask::getTaskStatus, queryVO.getTaskStatus())
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
    public RetryTaskResponseVO getRetryTaskById(Long id) {
        RetryTask retryTask = retryTaskMapper.selectById(id);

        if (retryTask == null) {
            return null;
        }

        Retry retry = retryMapper.selectById(retryTask.getRetryId());
        RetryTaskResponseVO responseVO = RetryTaskLogResponseVOConverter.INSTANCE.convert(retryTask);
        responseVO.setResponseVO(RetryTaskResponseVOConverter.INSTANCE.convert(retry));
        return responseVO;
    }

    @Override
    @Transactional
    public boolean deleteById(final Long id) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        RetryTask retryTask = retryTaskMapper.selectOne(
                new LambdaQueryWrapper<RetryTask>()
                        .in(RetryTask::getTaskStatus, RetryTaskStatusEnum.TERMINAL_STATUS_SET)
                        .eq(RetryTask::getNamespaceId, namespaceId)
                        .eq(RetryTask::getId, id));
        Assert.notNull(retryTask, () -> new SnailJobServerException("Data deletion failed"));

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
                        .in(RetryTask::getTaskStatus, RetryTaskStatusEnum.TERMINAL_STATUS_SET)
                        .eq(RetryTask::getNamespaceId, namespaceId)
                        .in(RetryTask::getId, ids));
        Assert.notEmpty(retryTasks, () -> new SnailJobServerException("Data does not exist"));
        Assert.isTrue(retryTasks.size() == ids.size(), () -> new SnailJobServerException("Data does not exist"));

        for (final RetryTask retryTask : retryTasks) {
            retryTaskLogMessageMapper.delete(
                    new LambdaQueryWrapper<RetryTaskLogMessage>()
                            .eq(RetryTaskLogMessage::getNamespaceId, namespaceId)
                            .eq(RetryTaskLogMessage::getGroupName, retryTask.getGroupName())
                            .eq(RetryTaskLogMessage::getRetryTaskId, retryTask.getId()));
        }
        return 1 == retryTaskMapper.deleteByIds(ids);
    }

    @Override
    public Boolean stopById(Long id) {

        RetryTask retryTask = retryTaskMapper.selectById(id);
        Assert.notNull(retryTask, () -> new SnailJobServerException("No executable tasks"));

        Retry retry = retryMapper.selectById(retryTask.getRetryId());
        Assert.notNull(retry, () -> new SnailJobServerException("Task does not exist"));

        TaskStopJobDTO taskStopJobDTO = RetryConverter.INSTANCE.toTaskStopJobDTO(retry);
        taskStopJobDTO.setOperationReason(RetryOperationReasonEnum.MANNER_STOP.getReason());
        taskStopJobDTO.setNeedUpdateTaskStatus(true);
        taskStopJobDTO.setMessage("User manually triggered stop");
        retryTaskStopHandler.stop(taskStopJobDTO);

        return true;
    }
}
