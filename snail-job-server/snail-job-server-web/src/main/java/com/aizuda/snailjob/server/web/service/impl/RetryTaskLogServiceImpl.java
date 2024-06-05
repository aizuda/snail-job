package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.constant.LogFieldConstants;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.RetryTaskLogMessageQueryVO;
import com.aizuda.snailjob.server.web.model.request.RetryTaskLogQueryVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.response.RetryTaskLogMessageResponseVO;
import com.aizuda.snailjob.server.web.model.response.RetryTaskLogResponseVO;
import com.aizuda.snailjob.server.web.service.RetryTaskLogService;
import com.aizuda.snailjob.server.web.service.convert.RetryTaskLogResponseVOConverter;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLog;
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

    private final RetryTaskLogMapper retryTaskLogMapper;
    private final RetryTaskLogMessageMapper retryTaskLogMessageMapper;

    @Override
    public PageResult<List<RetryTaskLogResponseVO>> getRetryTaskLogPage(RetryTaskLogQueryVO queryVO) {

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        String namespaceId = userSessionVO.getNamespaceId();
        PageDTO<RetryTaskLog> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        LambdaQueryWrapper<RetryTaskLog> retryTaskLogLambdaQueryWrapper = new LambdaQueryWrapper<RetryTaskLog>()
                .eq(RetryTaskLog::getNamespaceId, namespaceId)
                .in(userSessionVO.isUser(), RetryTaskLog::getGroupName, userSessionVO.getGroupNames())
                .eq(StrUtil.isNotBlank(queryVO.getGroupName()), RetryTaskLog::getGroupName, queryVO.getGroupName())
                .eq(StrUtil.isNotBlank(queryVO.getSceneName()), RetryTaskLog::getSceneName, queryVO.getSceneName())
                .eq(StrUtil.isNotBlank(queryVO.getBizNo()), RetryTaskLog::getBizNo, queryVO.getBizNo())
                .eq(StrUtil.isNotBlank(queryVO.getUniqueId()), RetryTaskLog::getUniqueId, queryVO.getUniqueId())
                .eq(StrUtil.isNotBlank(queryVO.getIdempotentId()), RetryTaskLog::getIdempotentId, queryVO.getIdempotentId())
                .eq(queryVO.getRetryStatus() != null, RetryTaskLog::getRetryStatus, queryVO.getRetryStatus())
                .select(RetryTaskLog::getGroupName, RetryTaskLog::getId,
                        RetryTaskLog::getSceneName,
                        RetryTaskLog::getIdempotentId, RetryTaskLog::getBizNo, RetryTaskLog::getRetryStatus,
                        RetryTaskLog::getCreateDt, RetryTaskLog::getUniqueId, RetryTaskLog::getTaskType)
                .orderByDesc(RetryTaskLog::getCreateDt);
        PageDTO<RetryTaskLog> retryTaskLogPageDTO = retryTaskLogMapper.selectPage(pageDTO,
                retryTaskLogLambdaQueryWrapper);

        return new PageResult<>(
                retryTaskLogPageDTO,
                RetryTaskLogResponseVOConverter.INSTANCE.convertList(retryTaskLogPageDTO.getRecords()));
    }

    @Override
    public RetryTaskLogMessageResponseVO getRetryTaskLogMessagePage(
            RetryTaskLogMessageQueryVO queryVO) {
        if (StrUtil.isBlank(queryVO.getUniqueId()) || StrUtil.isBlank(queryVO.getGroupName())) {
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
                        .eq(RetryTaskLogMessage::getUniqueId, queryVO.getUniqueId())
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
        RetryTaskLog retryTaskLog = retryTaskLogMapper.selectById(id);
        return RetryTaskLogResponseVOConverter.INSTANCE.convert(retryTaskLog);
    }

    @Override
    @Transactional
    public boolean deleteById(final Long id) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        RetryTaskLog retryTaskLog = retryTaskLogMapper.selectOne(
                new LambdaQueryWrapper<RetryTaskLog>().eq(RetryTaskLog::getRetryStatus, RetryStatusEnum.FINISH.getStatus())
                        .eq(RetryTaskLog::getNamespaceId, namespaceId)
                        .eq(RetryTaskLog::getId, id));
        Assert.notNull(retryTaskLog, () -> new SnailJobServerException("数据删除失败"));

        retryTaskLogMessageMapper.delete(new LambdaQueryWrapper<RetryTaskLogMessage>()
                .eq(RetryTaskLogMessage::getNamespaceId, namespaceId)
                .eq(RetryTaskLogMessage::getGroupName, retryTaskLog.getGroupName())
                .eq(RetryTaskLogMessage::getUniqueId, retryTaskLog.getUniqueId())
        );

        return 1 == retryTaskLogMapper.deleteById(id);
    }

    @Override
    @Transactional
    public boolean batchDelete(final Set<Long> ids) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        List<RetryTaskLog> retryTaskLogs = retryTaskLogMapper.selectList(
                new LambdaQueryWrapper<RetryTaskLog>()
                        .eq(RetryTaskLog::getRetryStatus, RetryStatusEnum.FINISH.getStatus())
                        .eq(RetryTaskLog::getNamespaceId, namespaceId)
                        .in(RetryTaskLog::getId, ids));
        Assert.notEmpty(retryTaskLogs, () -> new SnailJobServerException("数据不存在"));
        Assert.isTrue(retryTaskLogs.size() == ids.size(), () -> new SnailJobServerException("数据不存在"));

        for (final RetryTaskLog retryTaskLog : retryTaskLogs) {
            retryTaskLogMessageMapper.delete(
                    new LambdaQueryWrapper<RetryTaskLogMessage>()
                            .eq(RetryTaskLogMessage::getNamespaceId, namespaceId)
                            .eq(RetryTaskLogMessage::getGroupName, retryTaskLog.getGroupName())
                            .eq(RetryTaskLogMessage::getUniqueId, retryTaskLog.getUniqueId()));
        }
        return 1 == retryTaskLogMapper.deleteBatchIds(ids);
    }
}
