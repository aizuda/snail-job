package com.aizuda.easy.retry.server.service.impl;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryDeadLetterMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryDeadLetter;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.service.convert.RetryDeadLetterResponseVOConverter;
import com.aizuda.easy.retry.server.support.strategy.WaitStrategies;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.aizuda.easy.retry.server.config.RequestDataHelper;
import com.aizuda.easy.retry.server.service.RetryDeadLetterService;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.RetryDeadLetterQueryVO;
import com.aizuda.easy.retry.server.web.model.response.RetryDeadLetterResponseVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-28 09:46
 */
@Service
public class RetryDeadLetterServiceImpl implements RetryDeadLetterService {

    @Autowired
    private RetryDeadLetterMapper retryDeadLetterMapper;

    @Autowired
    private RetryTaskMapper retryTaskMapper;

    @Override
    public PageResult<List<RetryDeadLetterResponseVO>> getRetryDeadLetterPage(RetryDeadLetterQueryVO queryVO) {

        PageDTO<RetryDeadLetter> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        if (StringUtils.isBlank(queryVO.getGroupName()))  {
            return new PageResult<>(pageDTO, new ArrayList<>());
        }

        LambdaQueryWrapper<RetryDeadLetter> retryDeadLetterLambdaQueryWrapper = new LambdaQueryWrapper<>();
        retryDeadLetterLambdaQueryWrapper.eq(RetryDeadLetter::getGroupName, queryVO.getGroupName());

        if (StringUtils.isNotBlank(queryVO.getSceneName())) {
            retryDeadLetterLambdaQueryWrapper.eq(RetryDeadLetter::getSceneName, queryVO.getSceneName());
        }

        if (StringUtils.isNotBlank(queryVO.getBizNo())) {
            retryDeadLetterLambdaQueryWrapper.eq(RetryDeadLetter::getBizNo, queryVO.getBizNo());
        }

        if (StringUtils.isNotBlank(queryVO.getIdempotentId())) {
            retryDeadLetterLambdaQueryWrapper.eq(RetryDeadLetter::getIdempotentId, queryVO.getIdempotentId());
        }

        RequestDataHelper.setPartition(queryVO.getGroupName());
        PageDTO<RetryDeadLetter> retryDeadLetterPageDTO = retryDeadLetterMapper.selectPage(pageDTO, retryDeadLetterLambdaQueryWrapper);

        return new PageResult<>(retryDeadLetterPageDTO,
                RetryDeadLetterResponseVOConverter.INSTANCE.batchConvert(retryDeadLetterPageDTO.getRecords()));
    }

    @Override
    public RetryDeadLetterResponseVO getRetryDeadLetterById(String groupName, Long id) {
        RequestDataHelper.setPartition(groupName);

        RetryDeadLetter retryDeadLetter = retryDeadLetterMapper.selectById(id);
        return RetryDeadLetterResponseVOConverter.INSTANCE.convert(retryDeadLetter);
    }

    @Override
    @Transactional
    public boolean rollback(String groupName, Long id) {
        RequestDataHelper.setPartition(groupName);
        RetryDeadLetter retryDeadLetter = retryDeadLetterMapper.selectById(id);

        RetryTask retryTask = new RetryTask();
        BeanUtils.copyProperties(retryDeadLetter, retryTask);
        retryTask.setNextTriggerAt(WaitStrategies.randomWait(1, TimeUnit.SECONDS, 60, TimeUnit.SECONDS).computeRetryTime(null));
        retryTask.setCreateDt(LocalDateTime.now());
        retryTask.setUpdateDt(LocalDateTime.now());
        retryTask.setId(null);
        RequestDataHelper.setPartition(groupName);
        Assert.isTrue(1 == retryTaskMapper.insert(retryTask), () -> new EasyRetryServerException("新增重试任务失败"));

        RequestDataHelper.setPartition(groupName);
        Assert.isTrue(1 == retryDeadLetterMapper.deleteById(retryDeadLetter.getId()), () -> new EasyRetryServerException("删除死信队列数据失败"));

        return true;
    }

    @Override
    public boolean deleteById(String groupName, Long id) {
        RequestDataHelper.setPartition(groupName);
        return retryDeadLetterMapper.deleteById(id) == 1;
    }
}
