package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies.WaitStrategyContext;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies.WaitStrategyEnum;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.BatchDeleteRetryDeadLetterVO;
import com.aizuda.snailjob.server.web.model.request.BatchRollBackRetryDeadLetterVO;
import com.aizuda.snailjob.server.web.model.request.RetryDeadLetterQueryVO;
import com.aizuda.snailjob.server.web.model.response.RetryDeadLetterResponseVO;
import com.aizuda.snailjob.server.web.service.RetryDeadLetterService;
import com.aizuda.snailjob.server.web.service.convert.RetryDeadLetterResponseVOConverter;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.ConfigAccess;
import com.aizuda.snailjob.template.datasource.access.TaskAccess;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.*;
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

import static com.aizuda.snailjob.common.core.enums.RetryStatusEnum.ALLOW_DELETE_STATUS;

/**
 * @author: opensnail
 * @date : 2022-02-28 09:46
 */
@Service
public class RetryDeadLetterServiceImpl implements RetryDeadLetterService {

    @Autowired
    private AccessTemplate accessTemplate;
    @Autowired
    private RetryTaskMapper retryTaskMapper;
    @Autowired
    private RetryTaskLogMessageMapper retryTaskLogMessageMapper;

    @Override
    public PageResult<List<RetryDeadLetterResponseVO>> getRetryDeadLetterPage(RetryDeadLetterQueryVO queryVO) {

        PageDTO<RetryDeadLetter> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        List<String> groupNames = UserSessionUtils.getGroupNames(queryVO.getGroupName());

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        PageDTO<RetryDeadLetter> retryDeadLetterPageDTO = accessTemplate.getRetryDeadLetterAccess()
                .listPage(pageDTO, new LambdaQueryWrapper<RetryDeadLetter>()
                                .eq(RetryDeadLetter::getNamespaceId, namespaceId)
                                .in(CollUtil.isNotEmpty(groupNames), RetryDeadLetter::getGroupName, groupNames)
                                .eq(StrUtil.isNotBlank(queryVO.getSceneName()), RetryDeadLetter::getSceneName, queryVO.getSceneName())
                                .eq(StrUtil.isNotBlank(queryVO.getBizNo()), RetryDeadLetter::getBizNo, queryVO.getBizNo())
                                .eq(StrUtil.isNotBlank(queryVO.getIdempotentId()), RetryDeadLetter::getIdempotentId, queryVO.getIdempotentId())
                                .between(ObjUtil.isAllNotEmpty(queryVO.getStartDt(), queryVO.getEndDt()),
                                        RetryDeadLetter::getCreateDt, queryVO.getStartDt(), queryVO.getEndDt())
                                .orderByDesc(RetryDeadLetter::getId));

        return new PageResult<>(retryDeadLetterPageDTO,
                RetryDeadLetterResponseVOConverter.INSTANCE.convertList(retryDeadLetterPageDTO.getRecords()));
    }

    @Override
    public RetryDeadLetterResponseVO getRetryDeadLetterById(String groupName, Long id) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        TaskAccess<RetryDeadLetter> retryDeadLetterAccess = accessTemplate.getRetryDeadLetterAccess();
        RetryDeadLetter retryDeadLetter = retryDeadLetterAccess.one(new LambdaQueryWrapper<RetryDeadLetter>().eq(RetryDeadLetter::getId, id));
        return RetryDeadLetterResponseVOConverter.INSTANCE.convert(retryDeadLetter);
    }

    @Override
    @Transactional
    public int rollback(BatchRollBackRetryDeadLetterVO rollBackRetryDeadLetterVO) {

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        List<Long> ids = rollBackRetryDeadLetterVO.getIds();
        TaskAccess<RetryDeadLetter> retryDeadLetterAccess = accessTemplate.getRetryDeadLetterAccess();
        List<RetryDeadLetter> retryDeadLetterList = retryDeadLetterAccess.list(
                new LambdaQueryWrapper<RetryDeadLetter>().in(RetryDeadLetter::getId, ids));

        Assert.notEmpty(retryDeadLetterList, () -> new SnailJobServerException("数据不存在"));

        ConfigAccess<RetrySceneConfig> sceneConfigAccess = accessTemplate.getSceneConfigAccess();
        Set<String> sceneNameSet = StreamUtils.toSet(retryDeadLetterList, RetryDeadLetter::getSceneName);
        List<RetrySceneConfig> retrySceneConfigs = sceneConfigAccess.list(
                new LambdaQueryWrapper<RetrySceneConfig>()
                        .eq(RetrySceneConfig::getNamespaceId, namespaceId)
                        .in(RetrySceneConfig::getSceneName, sceneNameSet));

        Map<String, RetrySceneConfig> sceneConfigMap = StreamUtils.toIdentityMap(retrySceneConfigs,
                (sceneConfig) -> sceneConfig.getGroupName() + sceneConfig.getSceneName());

        List<Retry> waitRollbackList = new ArrayList<>();
        for (RetryDeadLetter retryDeadLetter : retryDeadLetterList) {
            RetrySceneConfig retrySceneConfig = sceneConfigMap.get(
                    retryDeadLetter.getGroupName() + retryDeadLetter.getSceneName());
            Assert.notNull(retrySceneConfig,
                    () -> new SnailJobServerException("未查询到场景. [{}]", retryDeadLetter.getSceneName()));

            Retry retry = RetryTaskConverter.INSTANCE.toRetryTask(retryDeadLetter);
            retry.setRetryStatus(RetryStatusEnum.RUNNING.getStatus());
            retry.setTaskType(SyetemTaskTypeEnum.RETRY.getType());

            WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
            waitStrategyContext.setNextTriggerAt(LocalDateTime.now());
            waitStrategyContext.setTriggerInterval(retrySceneConfig.getTriggerInterval());
            waitStrategyContext.setDelayLevel(1);
            WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getBackOff());
            retry.setNextTriggerAt(waitStrategy.computeTriggerTime(waitStrategyContext));
            retry.setCreateDt(LocalDateTime.now());
            waitRollbackList.add(retry);
        }

        TaskAccess<Retry> retryTaskAccess = accessTemplate.getRetryAccess();
        Assert.isTrue(waitRollbackList.size() == retryTaskAccess.insertBatch( waitRollbackList),
                () -> new SnailJobServerException("新增重试任务失败"));

        Set<Long> waitDelRetryDeadLetterIdSet = StreamUtils.toSet(retryDeadLetterList, RetryDeadLetter::getId);
        Assert.isTrue(waitDelRetryDeadLetterIdSet.size() == retryDeadLetterAccess.delete(
                        new LambdaQueryWrapper<RetryDeadLetter>()
                                .in(RetryDeadLetter::getId, waitDelRetryDeadLetterIdSet)),
                () -> new SnailJobServerException("删除死信队列数据失败"));

        // 变更日志的状态
        RetryTask retryTask = new RetryTask();
        retryTask.setTaskStatus(RetryStatusEnum.RUNNING.getStatus());
        return 1;
    }

    @Override
    public boolean batchDelete(BatchDeleteRetryDeadLetterVO deadLetterVO) {
        TaskAccess<RetryDeadLetter> retryDeadLetterAccess = accessTemplate.getRetryDeadLetterAccess();
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        Assert.isTrue(deadLetterVO.getIds().size() == retryDeadLetterAccess.delete(
                        new LambdaQueryWrapper<RetryDeadLetter>()
                                .eq(RetryDeadLetter::getNamespaceId, namespaceId)
                                .in(RetryDeadLetter::getId, deadLetterVO.getIds())),
                () -> new SnailJobServerException("删除死信任务失败"));

        return Boolean.TRUE;
    }
}
