package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.constant.LogFieldConstants;
import com.aizuda.easy.retry.server.web.model.request.JobLogQueryVO;
import com.aizuda.easy.retry.server.web.model.request.UserSessionVO;
import com.aizuda.easy.retry.server.web.model.response.JobLogResponseVO;
import com.aizuda.easy.retry.server.web.util.UserSessionUtils;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobLogMessage;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLogMessage;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskLogMessageQueryVO;
import com.aizuda.easy.retry.server.web.model.response.RetryTaskLogMessageResponseVO;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.service.RetryTaskLogService;
import com.aizuda.easy.retry.server.web.service.convert.RetryTaskLogResponseVOConverter;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskLogQueryVO;
import com.aizuda.easy.retry.server.web.model.response.RetryTaskLogResponseVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-28 09:10
 */
@Service
@RequiredArgsConstructor
public class RetryTaskLogServiceImpl implements RetryTaskLogService {

    private final RetryTaskLogMapper retryTaskLogMapper;
    private final RetryTaskLogMessageMapper retryTaskLogMessageMapper;

    @Override
    public PageResult<List<RetryTaskLogResponseVO>> getRetryTaskLogPage(RetryTaskLogQueryVO queryVO) {

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        String namespaceId = userSessionVO.getNamespaceId();
        PageDTO<RetryTaskLog> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        LambdaQueryWrapper<RetryTaskLog> retryTaskLogLambdaQueryWrapper = new LambdaQueryWrapper<>();
        retryTaskLogLambdaQueryWrapper.eq(RetryTaskLog::getNamespaceId, namespaceId);

        if (userSessionVO.isUser()) {
            retryTaskLogLambdaQueryWrapper.in(RetryTaskLog::getGroupName, userSessionVO.getGroupNames());
        }

        if (StrUtil.isNotBlank(queryVO.getGroupName())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLog::getGroupName, queryVO.getGroupName());
        }
        if (StrUtil.isNotBlank(queryVO.getSceneName())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLog::getSceneName, queryVO.getSceneName());
        }
        if (StrUtil.isNotBlank(queryVO.getBizNo())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLog::getBizNo, queryVO.getBizNo());
        }
        if (StrUtil.isNotBlank(queryVO.getUniqueId())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLog::getUniqueId, queryVO.getUniqueId());
        }
        if (StrUtil.isNotBlank(queryVO.getIdempotentId())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLog::getIdempotentId, queryVO.getIdempotentId());
        }

        retryTaskLogLambdaQueryWrapper.select(RetryTaskLog::getGroupName, RetryTaskLog::getId, RetryTaskLog::getSceneName,
                RetryTaskLog::getIdempotentId, RetryTaskLog::getBizNo, RetryTaskLog::getRetryStatus,
                RetryTaskLog::getCreateDt, RetryTaskLog::getUniqueId, RetryTaskLog::getTaskType);
        PageDTO<RetryTaskLog> retryTaskLogPageDTO = retryTaskLogMapper.selectPage(pageDTO, retryTaskLogLambdaQueryWrapper.orderByDesc(RetryTaskLog::getCreateDt));

        return new PageResult<>(
                retryTaskLogPageDTO,
                RetryTaskLogResponseVOConverter.INSTANCE.batchConvert(retryTaskLogPageDTO.getRecords()));
    }

    @Override
    public RetryTaskLogMessageResponseVO getRetryTaskLogMessagePage(
         RetryTaskLogMessageQueryVO queryVO) {

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        PageDTO<RetryTaskLogMessage> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        LambdaQueryWrapper<RetryTaskLogMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(RetryTaskLogMessage::getId, RetryTaskLogMessage::getLogNum);
        wrapper.ge(RetryTaskLogMessage::getId, queryVO.getStartId());
        wrapper.eq(RetryTaskLogMessage::getNamespaceId, namespaceId);
        wrapper.eq(RetryTaskLogMessage::getUniqueId, queryVO.getUniqueId());
        wrapper.eq(RetryTaskLogMessage::getGroupName, queryVO.getGroupName());
        wrapper.orderByAsc(RetryTaskLogMessage::getId).orderByAsc(RetryTaskLogMessage::getRealTime);

        PageDTO<RetryTaskLogMessage> selectPage = retryTaskLogMessageMapper.selectPage(pageDTO, wrapper.orderByDesc(RetryTaskLogMessage::getCreateDt));

        List<RetryTaskLogMessage> records = selectPage.getRecords();

        if (CollectionUtils.isEmpty(records)) {

            RetryTaskLogMessageResponseVO jobLogResponseVO = new RetryTaskLogMessageResponseVO();

            jobLogResponseVO.setFinished(Boolean.TRUE);
            jobLogResponseVO.setNextStartId(queryVO.getStartId());
            jobLogResponseVO.setFromIndex(0);
            return jobLogResponseVO;
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
                fromIndex = Math.min(fromIndex + queryVO.getSize(), originalList.size() - 1);
                break;
            }

            messages.addAll(pageList);
            nextStartId = retryTaskLogMessage.getId() + 1;
            fromIndex = 0;
        }

        messages = messages.stream().sorted((o1, o2) -> {
            long value = Long.parseLong(o1.get(LogFieldConstants.TIME_STAMP)) - Long.parseLong(o2.get(LogFieldConstants.TIME_STAMP));

            if (value > 0) {
                return 1;
            } else if (value < 0) {
                return -1;
            }

            return 0;
        }).collect(Collectors.toList());

        RetryTaskLogMessageResponseVO responseVO = new RetryTaskLogMessageResponseVO();
        responseVO.setMessage(messages);
        responseVO.setNextStartId(nextStartId);
        responseVO.setFromIndex(fromIndex);

        return responseVO;
    }

    @Override
    public RetryTaskLogResponseVO getRetryTaskLogById(Long id) {
        RetryTaskLog retryTaskLog = retryTaskLogMapper.selectById(id);
        return RetryTaskLogResponseVOConverter.INSTANCE.convert(retryTaskLog);
    }
}
