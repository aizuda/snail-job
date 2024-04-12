package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;

import com.aizuda.easy.retry.server.common.WaitStrategy;
import com.aizuda.easy.retry.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.strategy.WaitStrategies.WaitStrategyContext;
import com.aizuda.easy.retry.server.common.strategy.WaitStrategies.WaitStrategyEnum;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.web.service.RetryDeadLetterService;
import com.aizuda.easy.retry.server.web.service.convert.RetryDeadLetterResponseVOConverter;
import com.aizuda.easy.retry.server.retry.task.support.RetryTaskConverter;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.BatchDeleteRetryDeadLetterVO;
import com.aizuda.easy.retry.server.web.model.request.BatchRollBackRetryDeadLetterVO;
import com.aizuda.easy.retry.server.web.model.request.RetryDeadLetterQueryVO;
import com.aizuda.easy.retry.server.web.model.response.RetryDeadLetterResponseVO;
import com.aizuda.easy.retry.server.web.util.UserSessionUtils;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.access.ConfigAccess;
import com.aizuda.easy.retry.template.datasource.access.TaskAccess;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryDeadLetter;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: opensnail
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

        if (StrUtil.isBlank(queryVO.getGroupName())) {
            return new PageResult<>(pageDTO, new ArrayList<>());
        }

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        LambdaQueryWrapper<RetryDeadLetter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RetryDeadLetter::getNamespaceId, namespaceId);
        queryWrapper.eq(RetryDeadLetter::getGroupName, queryVO.getGroupName());

        if (StrUtil.isNotBlank(queryVO.getSceneName())) {
            queryWrapper.eq(RetryDeadLetter::getSceneName, queryVO.getSceneName());
        }

        if (StrUtil.isNotBlank(queryVO.getBizNo())) {
            queryWrapper.eq(RetryDeadLetter::getBizNo, queryVO.getBizNo());
        }

        if (StrUtil.isNotBlank(queryVO.getIdempotentId())) {
            queryWrapper.eq(RetryDeadLetter::getIdempotentId, queryVO.getIdempotentId());
        }

        if (StrUtil.isNotBlank(queryVO.getUniqueId())) {
            queryWrapper.eq(RetryDeadLetter::getUniqueId, queryVO.getUniqueId());
        }

        PageDTO<RetryDeadLetter> retryDeadLetterPageDTO = accessTemplate.getRetryDeadLetterAccess()
                .listPage(queryVO.getGroupName(), namespaceId, pageDTO, queryWrapper);

        return new PageResult<>(retryDeadLetterPageDTO,
                RetryDeadLetterResponseVOConverter.INSTANCE.batchConvert(retryDeadLetterPageDTO.getRecords()));
    }

    @Override
    public RetryDeadLetterResponseVO getRetryDeadLetterById(String groupName, Long id) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        TaskAccess<RetryDeadLetter> retryDeadLetterAccess = accessTemplate.getRetryDeadLetterAccess();
        RetryDeadLetter retryDeadLetter = retryDeadLetterAccess.one(groupName, namespaceId,
                new LambdaQueryWrapper<RetryDeadLetter>().eq(RetryDeadLetter::getId, id));
        return RetryDeadLetterResponseVOConverter.INSTANCE.convert(retryDeadLetter);
    }

    @Override
    @Transactional
    public int rollback(BatchRollBackRetryDeadLetterVO rollBackRetryDeadLetterVO) {

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        String groupName = rollBackRetryDeadLetterVO.getGroupName();
        List<Long> ids = rollBackRetryDeadLetterVO.getIds();
        TaskAccess<RetryDeadLetter> retryDeadLetterAccess = accessTemplate.getRetryDeadLetterAccess();
        List<RetryDeadLetter> retryDeadLetterList = retryDeadLetterAccess.list(groupName, namespaceId,
                new LambdaQueryWrapper<RetryDeadLetter>().in(RetryDeadLetter::getId, ids));

        Assert.notEmpty(retryDeadLetterList, () -> new EasyRetryServerException("数据不存在"));

        ConfigAccess<SceneConfig> sceneConfigAccess = accessTemplate.getSceneConfigAccess();
        Set<String> sceneNameSet = retryDeadLetterList.stream().map(RetryDeadLetter::getSceneName)
                .collect(Collectors.toSet());
        List<SceneConfig> sceneConfigs = sceneConfigAccess.list(new LambdaQueryWrapper<SceneConfig>()
                .eq(SceneConfig::getNamespaceId, namespaceId)
                .in(SceneConfig::getSceneName, sceneNameSet));

        Map<String, SceneConfig> sceneConfigMap = sceneConfigs.stream().collect(Collectors.toMap((sceneConfig) ->
                sceneConfig.getGroupName() + sceneConfig.getSceneName(), Function.identity()));

        List<RetryTask> waitRollbackList = new ArrayList<>();
        for (RetryDeadLetter retryDeadLetter : retryDeadLetterList) {
            SceneConfig sceneConfig = sceneConfigMap.get(
                    retryDeadLetter.getGroupName() + retryDeadLetter.getSceneName());
            Assert.notNull(sceneConfig,
                    () -> new EasyRetryServerException("未查询到场景. [{}]", retryDeadLetter.getSceneName()));

            RetryTask retryTask = RetryTaskConverter.INSTANCE.toRetryTask(retryDeadLetter);
            retryTask.setRetryStatus(RetryStatusEnum.RUNNING.getStatus());
            retryTask.setTaskType(SyetemTaskTypeEnum.RETRY.getType());

            WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
            waitStrategyContext.setNextTriggerAt(LocalDateTime.now());
            waitStrategyContext.setTriggerInterval(sceneConfig.getTriggerInterval());
            waitStrategyContext.setDelayLevel(1);
            WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(sceneConfig.getBackOff());
            retryTask.setNextTriggerAt(DateUtils.toLocalDateTime(waitStrategy.computeTriggerTime(waitStrategyContext)));
            retryTask.setCreateDt(LocalDateTime.now());
            waitRollbackList.add(retryTask);
        }

        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        Assert.isTrue(waitRollbackList.size() == retryTaskAccess.batchInsert(groupName, namespaceId, waitRollbackList),
                () -> new EasyRetryServerException("新增重试任务失败"));

        Set<Long> waitDelRetryDeadLetterIdSet = retryDeadLetterList.stream().map(RetryDeadLetter::getId)
                .collect(Collectors.toSet());
        Assert.isTrue(waitDelRetryDeadLetterIdSet.size() == retryDeadLetterAccess.delete(groupName, namespaceId,
                        new LambdaQueryWrapper<RetryDeadLetter>()
                                .eq(RetryDeadLetter::getGroupName, groupName)
                                .in(RetryDeadLetter::getId, waitDelRetryDeadLetterIdSet)),
                () -> new EasyRetryServerException("删除死信队列数据失败"))
        ;

        // 变更日志的状态
        RetryTaskLog retryTaskLog = new RetryTaskLog();
        retryTaskLog.setRetryStatus(RetryStatusEnum.RUNNING.getStatus());

        Set<String> uniqueIdSet = waitRollbackList.stream().map(RetryTask::getUniqueId).collect(Collectors.toSet());
        int update = retryTaskLogMapper.update(retryTaskLog, new LambdaUpdateWrapper<RetryTaskLog>()
                .eq(RetryTaskLog::getNamespaceId, namespaceId)
                .in(RetryTaskLog::getUniqueId, uniqueIdSet)
                .eq(RetryTaskLog::getGroupName, groupName));
        Assert.isTrue(update == uniqueIdSet.size(),
                () -> new EasyRetryServerException("回滚日志状态失败, 可能原因: 日志信息缺失或存在多个相同uniqueId"));

        return update;
    }

    @Override
    public int batchDelete(BatchDeleteRetryDeadLetterVO deadLetterVO) {
        TaskAccess<RetryDeadLetter> retryDeadLetterAccess = accessTemplate.getRetryDeadLetterAccess();
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        return retryDeadLetterAccess.delete(deadLetterVO.getGroupName(), namespaceId,
                new LambdaQueryWrapper<RetryDeadLetter>()
                        .eq(RetryDeadLetter::getNamespaceId, namespaceId)
                        .eq(RetryDeadLetter::getGroupName, deadLetterVO.getGroupName())
                        .in(RetryDeadLetter::getId, deadLetterVO.getIds()));
    }
}
