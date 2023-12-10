package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.server.web.model.request.UserSessionVO;
import com.aizuda.easy.retry.server.web.util.UserSessionUtils;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-28 09:10
 */
@Service
public class RetryTaskLogServiceImpl implements RetryTaskLogService {

    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;
    @Autowired
    private RetryTaskLogMessageMapper retryTaskLogMessageMapper;

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
    public PageResult<List<RetryTaskLogMessageResponseVO>> getRetryTaskLogMessagePage(
         RetryTaskLogMessageQueryVO queryVO) {

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        PageDTO<RetryTaskLogMessage> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        LambdaQueryWrapper<RetryTaskLogMessage> retryTaskLogLambdaQueryWrapper = new LambdaQueryWrapper<>();
        retryTaskLogLambdaQueryWrapper.eq(RetryTaskLogMessage::getNamespaceId, namespaceId);

        if (StrUtil.isNotBlank(queryVO.getGroupName())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLogMessage::getGroupName, queryVO.getGroupName());
        }
        if (StrUtil.isNotBlank(queryVO.getUniqueId())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLogMessage::getUniqueId, queryVO.getUniqueId());
        }

        PageDTO<RetryTaskLogMessage> retryTaskLogPageDTO = retryTaskLogMessageMapper.selectPage(pageDTO, retryTaskLogLambdaQueryWrapper.orderByDesc(RetryTaskLogMessage::getCreateDt));

        return new PageResult<>(
            retryTaskLogPageDTO,
            RetryTaskLogResponseVOConverter.INSTANCE.toRetryTaskLogMessageResponseVO(retryTaskLogPageDTO.getRecords()));

    }

    @Override
    public RetryTaskLogResponseVO getRetryTaskLogById(Long id) {
        RetryTaskLog retryTaskLog = retryTaskLogMapper.selectById(id);
        return RetryTaskLogResponseVOConverter.INSTANCE.convert(retryTaskLog);
    }
}
