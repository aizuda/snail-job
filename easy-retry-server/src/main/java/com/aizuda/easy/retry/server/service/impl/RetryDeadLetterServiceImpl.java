package com.aizuda.easy.retry.server.service.impl;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryDeadLetterMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryDeadLetter;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.easy.retry.server.service.convert.RetryDeadLetterResponseVOConverter;
import com.aizuda.easy.retry.server.service.convert.RetryTaskConverter;
import com.aizuda.easy.retry.server.service.convert.RetryTaskLogConverter;
import com.aizuda.easy.retry.server.support.strategy.WaitStrategies;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.aizuda.easy.retry.template.datasource.utils.RequestDataHelper;
import com.aizuda.easy.retry.server.service.RetryDeadLetterService;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.RetryDeadLetterQueryVO;
import com.aizuda.easy.retry.server.web.model.response.RetryDeadLetterResponseVO;
import org.apache.commons.lang.StringUtils;
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
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;

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

        if (StringUtils.isNotBlank(queryVO.getUniqueId())) {
            retryDeadLetterLambdaQueryWrapper.eq(RetryDeadLetter::getUniqueId, queryVO.getUniqueId());
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
        Assert.notNull(retryDeadLetter, () -> new EasyRetryServerException("数据不存在"));

        RetryTask retryTask = RetryTaskConverter.INSTANCE.toRetryTask(retryDeadLetter);
        retryTask.setNextTriggerAt(WaitStrategies.randomWait(1, TimeUnit.SECONDS, 60, TimeUnit.SECONDS).computeRetryTime(null));
        retryTask.setCreateDt(LocalDateTime.now());
        retryTask.setUpdateDt(LocalDateTime.now());

        RequestDataHelper.setPartition(groupName);
        Assert.isTrue(1 == retryTaskMapper.insert(retryTask), () -> new EasyRetryServerException("新增重试任务失败"));

        RequestDataHelper.setPartition(groupName);
        Assert.isTrue(1 == retryDeadLetterMapper.deleteById(retryDeadLetter.getId()), () -> new EasyRetryServerException("删除死信队列数据失败"));

        // 变动日志的状态
        RetryTaskLog retryTaskLog = new RetryTaskLog();
        retryTaskLog.setRetryStatus(RetryStatusEnum.RUNNING.getStatus());
        int update = retryTaskLogMapper.update(retryTaskLog, new LambdaUpdateWrapper<RetryTaskLog>()
            .eq(RetryTaskLog::getUniqueId, retryTask.getUniqueId())
            .eq(RetryTaskLog::getGroupName, retryTask.getGroupName()));

        // 若日志不存在则初始化一个
        if (update == 0) {
            // 初始化回调日志
            retryTaskLog = RetryTaskLogConverter.INSTANCE.toRetryTask(retryTask);
            retryTaskLog.setTaskType(retryTask.getTaskType());
            retryTaskLog.setRetryStatus(RetryStatusEnum.RUNNING.getStatus());
            retryTaskLog.setCreateDt(LocalDateTime.now());
            Assert.isTrue(1 ==  retryTaskLogMapper.insert(retryTaskLog),
                () -> new EasyRetryServerException("新增重试日志失败"));
        }

        return true;
    }

    @Override
    public boolean deleteById(String groupName, Long id) {
        RequestDataHelper.setPartition(groupName);
        return retryDeadLetterMapper.deleteById(id) == 1;
    }
}
