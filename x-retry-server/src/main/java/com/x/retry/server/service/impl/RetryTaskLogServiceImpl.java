package com.x.retry.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.x.retry.server.web.model.base.PageResult;
import com.x.retry.server.persistence.mybatis.mapper.RetryTaskLogMapper;
import com.x.retry.server.persistence.mybatis.po.RetryTaskLog;
import com.x.retry.server.service.RetryTaskLogService;
import com.x.retry.server.service.convert.RetryTaskLogResponseVOConverter;
import com.x.retry.server.web.model.request.RetryTaskLogQueryVO;
import com.x.retry.server.web.model.response.RetryTaskLogResponseVO;
import org.apache.commons.lang.StringUtils;
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

    private RetryTaskLogResponseVOConverter retryTaskLogResponseVOConverter = new RetryTaskLogResponseVOConverter();
    @Override
    public PageResult<List<RetryTaskLogResponseVO>> getRetryTaskLogPage(RetryTaskLogQueryVO queryVO) {

        PageDTO<RetryTaskLog> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        LambdaQueryWrapper<RetryTaskLog> retryTaskLogLambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(queryVO.getGroupName())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLog::getGroupName, queryVO.getGroupName());
        }
        if (StringUtils.isNotBlank(queryVO.getSceneName())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLog::getSceneName, queryVO.getSceneName());
        }
        if (StringUtils.isNotBlank(queryVO.getBizNo())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLog::getBizNo, queryVO.getBizNo());
        }
        if (StringUtils.isNotBlank(queryVO.getBizId())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLog::getBizId, queryVO.getBizId());
        }

        retryTaskLogLambdaQueryWrapper.select(RetryTaskLog::getGroupName, RetryTaskLog::getId, RetryTaskLog::getSceneName,
                RetryTaskLog::getBizId, RetryTaskLog::getBizNo, RetryTaskLog::getErrorMessage, RetryTaskLog::getRetryStatus);
        PageDTO<RetryTaskLog> retryTaskLogPageDTO = retryTaskLogMapper.selectPage(pageDTO, retryTaskLogLambdaQueryWrapper.orderByDesc(RetryTaskLog::getCreateDt));

        return new PageResult<>(
                retryTaskLogPageDTO,
                retryTaskLogResponseVOConverter.batchConvert(retryTaskLogPageDTO.getRecords()));
    }

    @Override
    public RetryTaskLogResponseVO getRetryTaskLogById(Long id) {
        RetryTaskLog retryTaskLog = retryTaskLogMapper.selectById(id);
        return retryTaskLogResponseVOConverter.convert(retryTaskLog);
    }
}
