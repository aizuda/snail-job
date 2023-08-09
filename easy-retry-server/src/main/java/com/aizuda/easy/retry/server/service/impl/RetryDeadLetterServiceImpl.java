package com.aizuda.easy.retry.server.service.impl;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.service.RetryDeadLetterService;
import com.aizuda.easy.retry.server.service.convert.RetryDeadLetterResponseVOConverter;
import com.aizuda.easy.retry.server.service.convert.RetryTaskConverter;
import com.aizuda.easy.retry.server.support.strategy.WaitStrategies;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.BatchDeleteRetryDeadLetterVO;
import com.aizuda.easy.retry.server.web.model.request.BatchRollBackRetryDeadLetterVO;
import com.aizuda.easy.retry.server.web.model.request.RetryDeadLetterQueryVO;
import com.aizuda.easy.retry.server.web.model.response.RetryDeadLetterResponseVO;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.access.TaskAccess;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryDeadLetter;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-28 09:46
 */
@Service
public class RetryDeadLetterServiceImpl implements RetryDeadLetterService {

    @Autowired
    private AccessTemplate accessTemplate;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;

    @Override
    public PageResult<List<RetryDeadLetterResponseVO>> getRetryDeadLetterPage(RetryDeadLetterQueryVO queryVO) {

        PageDTO<RetryDeadLetter> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        if (StringUtils.isBlank(queryVO.getGroupName())) {
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

        PageDTO<RetryDeadLetter> retryDeadLetterPageDTO = accessTemplate.getRetryDeadLetterAccess()
                .listPage(queryVO.getGroupName(), pageDTO, retryDeadLetterLambdaQueryWrapper);

        return new PageResult<>(retryDeadLetterPageDTO,
                RetryDeadLetterResponseVOConverter.INSTANCE.batchConvert(retryDeadLetterPageDTO.getRecords()));
    }

    @Override
    public RetryDeadLetterResponseVO getRetryDeadLetterById(String groupName, Long id) {
        TaskAccess<RetryDeadLetter> retryDeadLetterAccess = accessTemplate.getRetryDeadLetterAccess();
        RetryDeadLetter retryDeadLetter = retryDeadLetterAccess.one(groupName,
                new LambdaQueryWrapper<RetryDeadLetter>().eq(RetryDeadLetter::getId, id));
        return RetryDeadLetterResponseVOConverter.INSTANCE.convert(retryDeadLetter);
    }

    @Override
    @Transactional
    public boolean rollback(BatchRollBackRetryDeadLetterVO rollBackRetryDeadLetterVO) {

        String groupName = rollBackRetryDeadLetterVO.getGroupName();
        List<Long> ids = rollBackRetryDeadLetterVO.getIds();
        TaskAccess<RetryDeadLetter> retryDeadLetterAccess = accessTemplate.getRetryDeadLetterAccess();
        List<RetryDeadLetter> retryDeadLetterList = retryDeadLetterAccess.list(groupName,
                new LambdaQueryWrapper<RetryDeadLetter>().in(RetryDeadLetter::getId, ids));

        Assert.notEmpty(retryDeadLetterList, () -> new EasyRetryServerException("数据不存在"));

        List<RetryTask> waitRollbackList = new ArrayList<>();
        for (RetryDeadLetter retryDeadLetter : retryDeadLetterList) {
            RetryTask retryTask = RetryTaskConverter.INSTANCE.toRetryTask(retryDeadLetter);
            retryTask.setNextTriggerAt(WaitStrategies.randomWait(1, TimeUnit.SECONDS, 60, TimeUnit.SECONDS).computeRetryTime(null));
            retryTask.setCreateDt(LocalDateTime.now());
            retryTask.setUpdateDt(LocalDateTime.now());
            waitRollbackList.add(retryTask);
        }

        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        Assert.isTrue(waitRollbackList.size() == retryTaskAccess.batchInsert(groupName, waitRollbackList), () -> new EasyRetryServerException("新增重试任务失败"));

        Set<Long> waitDelRetryDeadLetterIdSet = retryDeadLetterList.stream().map(RetryDeadLetter::getId).collect(Collectors.toSet());
        Assert.isTrue(waitDelRetryDeadLetterIdSet.size() == retryDeadLetterAccess.delete(groupName, new LambdaQueryWrapper<RetryDeadLetter>()
                .eq(RetryDeadLetter::getGroupName, groupName)
                .in(RetryDeadLetter::getId, waitDelRetryDeadLetterIdSet)), () -> new EasyRetryServerException("删除死信队列数据失败"))
        ;

        // 变动日志的状态
        RetryTaskLog retryTaskLog = new RetryTaskLog();
        retryTaskLog.setRetryStatus(RetryStatusEnum.RUNNING.getStatus());

        Set<String> uniqueIdSet = waitRollbackList.stream().map(RetryTask::getUniqueId).collect(Collectors.toSet());
        int update = retryTaskLogMapper.update(retryTaskLog, new LambdaUpdateWrapper<RetryTaskLog>()
                .in(RetryTaskLog::getUniqueId, uniqueIdSet)
                .eq(RetryTaskLog::getGroupName, groupName));
        Assert.isTrue(update == uniqueIdSet.size(), () -> new EasyRetryServerException("回滚日志状态失败, 可能原因: 日志信息缺失"));

        return true;
    }

    @Override
    public boolean batchDelete(BatchDeleteRetryDeadLetterVO deadLetterVO) {
        TaskAccess<RetryDeadLetter> retryDeadLetterAccess = accessTemplate.getRetryDeadLetterAccess();
        return 1 == retryDeadLetterAccess.delete(deadLetterVO.getGroupName(),
                new LambdaQueryWrapper<RetryDeadLetter>()
                        .eq(RetryDeadLetter::getGroupName, deadLetterVO.getGroupName())
                        .in(RetryDeadLetter::getId, deadLetterVO.getIds()));
    }
}
