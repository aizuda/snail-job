package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.model.GenerateRetryIdempotentIdDTO;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.dto.RetryLogMetaDTO;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.enums.TaskGeneratorSceneEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.snailjob.server.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies.WaitStrategyContext;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies.WaitStrategyEnum;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.model.dto.RetryTaskDTO;
import com.aizuda.snailjob.server.retry.task.client.RetryRpcClient;
import com.aizuda.snailjob.server.retry.task.generator.task.TaskContext;
import com.aizuda.snailjob.server.retry.task.generator.task.TaskGenerator;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.dispatch.task.TaskExecutor;
import com.aizuda.snailjob.server.retry.task.support.dispatch.task.TaskExecutorSceneEnum;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.*;
import com.aizuda.snailjob.server.web.model.response.RetryTaskResponseVO;
import com.aizuda.snailjob.server.web.service.RetryTaskService;
import com.aizuda.snailjob.server.web.service.convert.RetryTaskResponseVOConverter;
import com.aizuda.snailjob.server.web.service.convert.TaskContextConverter;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.TaskAccess;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.aizuda.snailjob.common.core.enums.RetryStatusEnum.ALLOW_DELETE_STATUS;

/**
 * @author opensnail
 * @date 2022-02-27
 * @since 2.0
 */
@Service
public class RetryTaskServiceImpl implements RetryTaskService {

    @Autowired
    private ClientNodeAllocateHandler clientNodeAllocateHandler;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;
    @Autowired
    private AccessTemplate accessTemplate;
    @Autowired
    @Lazy
    private List<TaskGenerator> taskGenerators;
    @Lazy
    @Autowired
    private List<TaskExecutor> taskExecutors;
    @Autowired
    private RetryTaskLogMessageMapper retryTaskLogMessageMapper;

    @Override
    public PageResult<List<RetryTaskResponseVO>> getRetryTaskPage(RetryTaskQueryVO queryVO) {

        PageDTO<RetryTask> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        if (StrUtil.isBlank(queryVO.getGroupName())) {
            return new PageResult<>(pageDTO, new ArrayList<>());
        }

        List<String> groupNames = UserSessionUtils.getGroupNames(queryVO.getGroupName());

        LambdaQueryWrapper<RetryTask> queryWrapper = new LambdaQueryWrapper<RetryTask>()
                .eq(RetryTask::getNamespaceId, namespaceId)
                .in(CollUtil.isNotEmpty(groupNames), RetryTask::getGroupName, groupNames)
                .eq(StrUtil.isNotBlank(queryVO.getSceneName()), RetryTask::getSceneName, queryVO.getSceneName())
                .eq(StrUtil.isNotBlank(queryVO.getBizNo()), RetryTask::getBizNo, queryVO.getBizNo())
                .eq(StrUtil.isNotBlank(queryVO.getIdempotentId()), RetryTask::getIdempotentId, queryVO.getIdempotentId())
                .eq(StrUtil.isNotBlank(queryVO.getUniqueId()), RetryTask::getUniqueId, queryVO.getUniqueId())
                .eq(Objects.nonNull(queryVO.getRetryStatus()), RetryTask::getRetryStatus, queryVO.getRetryStatus())
                .select(RetryTask::getId, RetryTask::getBizNo, RetryTask::getIdempotentId,
                        RetryTask::getGroupName, RetryTask::getNextTriggerAt, RetryTask::getRetryCount,
                        RetryTask::getRetryStatus, RetryTask::getUpdateDt, RetryTask::getSceneName, RetryTask::getUniqueId,
                        RetryTask::getTaskType)
                .orderByDesc(RetryTask::getCreateDt);
        pageDTO = accessTemplate.getRetryTaskAccess().listPage(queryVO.getGroupName(), namespaceId, pageDTO, queryWrapper);
        return new PageResult<>(pageDTO,
                RetryTaskResponseVOConverter.INSTANCE.convertList(pageDTO.getRecords()));
    }

    @Override
    public RetryTaskResponseVO getRetryTaskById(String groupName, Long id) {
        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        RetryTask retryTask = retryTaskAccess.one(groupName, UserSessionUtils.currentUserSession().getNamespaceId(),
                new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getId, id));
        return RetryTaskResponseVOConverter.INSTANCE.convert(retryTask);
    }

    @Override
    @Transactional
    public int updateRetryTaskStatus(RetryTaskUpdateStatusRequestVO requestVO) {

        RetryStatusEnum retryStatusEnum = RetryStatusEnum.getByStatus(requestVO.getRetryStatus());
        if (Objects.isNull(retryStatusEnum)) {
            throw new SnailJobServerException("重试状态错误. [{}]", requestVO.getRetryStatus());
        }

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        RetryTask retryTask = retryTaskAccess.one(requestVO.getGroupName(), namespaceId,
                new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getNamespaceId, namespaceId)
                        .eq(RetryTask::getId, requestVO.getId()));
        if (Objects.isNull(retryTask)) {
            throw new SnailJobServerException("未查询到重试任务");
        }

        retryTask.setRetryStatus(requestVO.getRetryStatus());
        retryTask.setGroupName(requestVO.getGroupName());

        // 若恢复重试则需要重新计算下次触发时间
        if (RetryStatusEnum.RUNNING.getStatus().equals(retryStatusEnum.getStatus())) {

            RetrySceneConfig retrySceneConfig = accessTemplate.getSceneConfigAccess()
                    .getSceneConfigByGroupNameAndSceneName(retryTask.getGroupName(), retryTask.getSceneName(), namespaceId);
            WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
            waitStrategyContext.setNextTriggerAt(DateUtils.toNowMilli());
            waitStrategyContext.setTriggerInterval(retrySceneConfig.getTriggerInterval());
            waitStrategyContext.setDelayLevel(retryTask.getRetryCount() + 1);
            WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getBackOff());
            retryTask.setNextTriggerAt(DateUtils.toLocalDateTime(waitStrategy.computeTriggerTime(waitStrategyContext)));
        }

        if (RetryStatusEnum.FINISH.getStatus().equals(retryStatusEnum.getStatus())) {
            RetryLogMetaDTO retryLogMetaDTO = RetryTaskConverter.INSTANCE.toLogMetaDTO(retryTask);
            retryLogMetaDTO.setTimestamp(DateUtils.toNowMilli());
            SnailJobLog.REMOTE.info("=============手动操作完成============. <|>{}<|>", retryLogMetaDTO);
        }

        RetryTaskLog retryTaskLog = new RetryTaskLog();
        retryTaskLog.setRetryStatus(requestVO.getRetryStatus());
        retryTaskLogMapper.update(retryTaskLog, new LambdaUpdateWrapper<RetryTaskLog>()
                .eq(RetryTaskLog::getNamespaceId, namespaceId)
                .eq(RetryTaskLog::getUniqueId, retryTask.getUniqueId())
                .eq(RetryTaskLog::getGroupName, retryTask.getGroupName()));

        retryTask.setUpdateDt(LocalDateTime.now());
        return retryTaskAccess.updateById(requestVO.getGroupName(), namespaceId, retryTask);
    }

    @Override
    public int saveRetryTask(final RetryTaskSaveRequestVO retryTaskRequestVO) {
        RetryStatusEnum retryStatusEnum = RetryStatusEnum.getByStatus(retryTaskRequestVO.getRetryStatus());
        if (Objects.isNull(retryStatusEnum)) {
            throw new SnailJobServerException("重试状态错误");
        }

        TaskGenerator taskGenerator = taskGenerators.stream()
                .filter(t -> t.supports(TaskGeneratorSceneEnum.MANA_SINGLE.getScene()))
                .findFirst().orElseThrow(() -> new SnailJobServerException("没有匹配的任务生成器"));
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        TaskContext taskContext = new TaskContext();
        taskContext.setSceneName(retryTaskRequestVO.getSceneName());
        taskContext.setGroupName(retryTaskRequestVO.getGroupName());
        taskContext.setInitStatus(retryTaskRequestVO.getRetryStatus());
        taskContext.setNamespaceId(namespaceId);
        taskContext.setTaskInfos(
                Collections.singletonList(TaskContextConverter.INSTANCE.convert(retryTaskRequestVO)));

        // 生成任务
        taskGenerator.taskGenerator(taskContext);

        return 1;
    }

    @Override
    public String idempotentIdGenerate(final GenerateRetryIdempotentIdVO generateRetryIdempotentIdVO) {

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        Set<RegisterNodeInfo> serverNodes = CacheRegisterTable.getServerNodeSet(
                generateRetryIdempotentIdVO.getGroupName(),
                namespaceId);
        Assert.notEmpty(serverNodes,
                () -> new SnailJobServerException("生成idempotentId失败: 不存在活跃的客户端节点"));

        RetrySceneConfig retrySceneConfig = accessTemplate.getSceneConfigAccess()
                .getSceneConfigByGroupNameAndSceneName(generateRetryIdempotentIdVO.getGroupName(),
                        generateRetryIdempotentIdVO.getSceneName(), namespaceId);

        RegisterNodeInfo serverNode = clientNodeAllocateHandler.getServerNode(retrySceneConfig.getSceneName(),
                retrySceneConfig.getGroupName(), retrySceneConfig.getNamespaceId(), retrySceneConfig.getRouteKey());

        // 委托客户端生成idempotentId
        GenerateRetryIdempotentIdDTO generateRetryIdempotentIdDTO = new GenerateRetryIdempotentIdDTO();
        generateRetryIdempotentIdDTO.setGroup(generateRetryIdempotentIdVO.getGroupName());
        generateRetryIdempotentIdDTO.setScene(generateRetryIdempotentIdVO.getSceneName());
        generateRetryIdempotentIdDTO.setArgsStr(generateRetryIdempotentIdVO.getArgsStr());
        generateRetryIdempotentIdDTO.setExecutorName(generateRetryIdempotentIdVO.getExecutorName());

        RetryRpcClient rpcClient = RequestBuilder.<RetryRpcClient, Result>newBuilder()
                .nodeInfo(serverNode)
                .client(RetryRpcClient.class)
                .build();

        Result result = rpcClient.generateIdempotentId(generateRetryIdempotentIdDTO);

        Assert.notNull(result, () -> new SnailJobServerException("idempotentId生成失败"));
        Assert.isTrue(1 == result.getStatus(),
                () -> new SnailJobServerException("idempotentId生成失败:请确保参数与执行器名称正确"));

        return (String) result.getData();
    }

    @Override
    public int updateRetryTaskExecutorName(final RetryTaskUpdateExecutorNameRequestVO requestVO) {

        RetryTask retryTask = new RetryTask();
        retryTask.setExecutorName(requestVO.getExecutorName());
        retryTask.setRetryStatus(requestVO.getRetryStatus());
        retryTask.setUpdateDt(LocalDateTime.now());

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        // 根据重试数据id，更新执行器名称
        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        return retryTaskAccess.update(requestVO.getGroupName(), namespaceId, retryTask,
                new LambdaUpdateWrapper<RetryTask>()
                        .eq(RetryTask::getNamespaceId, namespaceId)
                        .eq(RetryTask::getGroupName, requestVO.getGroupName())
                        .in(RetryTask::getId, requestVO.getIds()));
    }

    @Override
    @Transactional
    public boolean batchDeleteRetryTask(final BatchDeleteRetryTaskVO requestVO) {
        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        List<RetryTask> tasks = retryTaskAccess.list(requestVO.getGroupName(), namespaceId,
                new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getNamespaceId, namespaceId)
                        .eq(RetryTask::getGroupName, requestVO.getGroupName())
                        .in(RetryTask::getRetryStatus, ALLOW_DELETE_STATUS)
                        .in(RetryTask::getId, requestVO.getIds())
        );

        Assert.notEmpty(tasks,
                () -> new SnailJobServerException("没有可删除的数据, 只有非【处理中】的数据可以删除"));

        Assert.isTrue(requestVO.getIds().size() == retryTaskAccess.delete(requestVO.getGroupName(), namespaceId,
                        new LambdaQueryWrapper<RetryTask>()
                                .eq(RetryTask::getNamespaceId, namespaceId)
                                .eq(RetryTask::getGroupName, requestVO.getGroupName())
                                .in(RetryTask::getRetryStatus, ALLOW_DELETE_STATUS)
                                .in(RetryTask::getId, requestVO.getIds()))
                , () -> new SnailJobServerException("删除重试任务失败, 请检查任务状态是否为已完成或者最大次数"));

        Set<String> uniqueIds = StreamUtils.toSet(tasks, RetryTask::getUniqueId);
        retryTaskLogMapper.delete(new LambdaQueryWrapper<RetryTaskLog>()
                .in(RetryTaskLog::getRetryStatus, ALLOW_DELETE_STATUS)
                .eq(RetryTaskLog::getGroupName, requestVO.getGroupName())
                .eq(RetryTaskLog::getNamespaceId, namespaceId)
                .in(RetryTaskLog::getUniqueId, uniqueIds));

        retryTaskLogMessageMapper.delete(
                new LambdaQueryWrapper<RetryTaskLogMessage>()
                        .eq(RetryTaskLogMessage::getNamespaceId, namespaceId)
                        .eq(RetryTaskLogMessage::getGroupName, requestVO.getGroupName())
                        .in(RetryTaskLogMessage::getUniqueId, uniqueIds));
        return Boolean.TRUE;
    }

    @Override
    public Integer parseLogs(ParseLogsVO parseLogsVO) {
        RetryStatusEnum retryStatusEnum = RetryStatusEnum.getByStatus(parseLogsVO.getRetryStatus());
        if (Objects.isNull(retryStatusEnum)) {
            throw new SnailJobServerException("重试状态错误");
        }

        String logStr = parseLogsVO.getLogStr();

        String patternString = "<\\|>(.*?)<\\|>";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(logStr);

        List<RetryTaskDTO> waitInsertList = new ArrayList<>();
        // 查找匹配的内容并输出
        while (matcher.find()) {
            String extractedData = matcher.group(1);
            if (StrUtil.isBlank(extractedData)) {
                continue;
            }

            List<RetryTaskDTO> retryTaskList = JsonUtil.parseList(extractedData, RetryTaskDTO.class);
            if (CollUtil.isNotEmpty(retryTaskList)) {
                waitInsertList.addAll(retryTaskList);
            }
        }

        Assert.isFalse(waitInsertList.isEmpty(), () -> new SnailJobServerException("未找到匹配的数据"));
        Assert.isTrue(waitInsertList.size() <= 500, () -> new SnailJobServerException("最多只能处理500条数据"));

        TaskGenerator taskGenerator = taskGenerators.stream()
                .filter(t -> t.supports(TaskGeneratorSceneEnum.MANA_BATCH.getScene()))
                .findFirst().orElseThrow(() -> new SnailJobServerException("没有匹配的任务生成器"));

        boolean allMatch = waitInsertList.stream()
                .allMatch(retryTaskDTO -> retryTaskDTO.getGroupName().equals(parseLogsVO.getGroupName()));
        Assert.isTrue(allMatch, () -> new SnailJobServerException("存在数据groupName不匹配，请检查您的数据"));

        Map<String, List<RetryTaskDTO>> map = StreamUtils.groupByKey(waitInsertList, RetryTaskDTO::getSceneName);

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        map.forEach(((sceneName, retryTaskDTOS) -> {
            TaskContext taskContext = new TaskContext();
            taskContext.setSceneName(sceneName);
            taskContext.setGroupName(parseLogsVO.getGroupName());
            taskContext.setNamespaceId(namespaceId);
            taskContext.setInitStatus(parseLogsVO.getRetryStatus());
            taskContext.setTaskInfos(TaskContextConverter.INSTANCE.convert(retryTaskDTOS));

            // 生成任务
            taskGenerator.taskGenerator(taskContext);
        }));

        return waitInsertList.size();
    }

    @Override
    public boolean manualTriggerRetryTask(ManualTriggerTaskRequestVO requestVO) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        long count = accessTemplate.getGroupConfigAccess().count(new LambdaQueryWrapper<GroupConfig>()
                .eq(GroupConfig::getGroupName, requestVO.getGroupName())
                .eq(GroupConfig::getNamespaceId, namespaceId)
                .eq(GroupConfig::getGroupStatus, StatusEnum.YES.getStatus())
        );

        Assert.isTrue(count > 0, () -> new SnailJobServerException("组:[{}]已经关闭，不支持手动执行.", requestVO.getGroupName()));

        List<String> uniqueIds = requestVO.getUniqueIds();


        List<RetryTask> list = accessTemplate.getRetryTaskAccess().list(
                requestVO.getGroupName(), namespaceId,
                new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getNamespaceId, namespaceId)
                        .eq(RetryTask::getTaskType, SyetemTaskTypeEnum.RETRY.getType())
                        .in(RetryTask::getUniqueId, uniqueIds));
        Assert.notEmpty(list, () -> new SnailJobServerException("没有可执行的任务"));

        for (RetryTask retryTask : list) {
            for (TaskExecutor taskExecutor : taskExecutors) {
                if (taskExecutor.getTaskType().getScene() == TaskExecutorSceneEnum.MANUAL_RETRY.getScene()) {
                    taskExecutor.actuator(retryTask);
                }
            }
        }

        return true;
    }

    @Override
    public boolean manualTriggerCallbackTask(ManualTriggerTaskRequestVO requestVO) {
        List<String> uniqueIds = requestVO.getUniqueIds();

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        long count = accessTemplate.getGroupConfigAccess().count(new LambdaQueryWrapper<GroupConfig>()
                .eq(GroupConfig::getGroupName, requestVO.getGroupName())
                .eq(GroupConfig::getNamespaceId, namespaceId)
                .eq(GroupConfig::getGroupStatus, StatusEnum.YES.getStatus())
        );

        Assert.isTrue(count > 0, () -> new SnailJobServerException("组:[{}]已经关闭，不支持手动执行.", requestVO.getGroupName()));

        List<RetryTask> list = accessTemplate.getRetryTaskAccess().list(requestVO.getGroupName(), namespaceId,
                new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getNamespaceId, namespaceId)
                        .eq(RetryTask::getTaskType, SyetemTaskTypeEnum.CALLBACK.getType())
                        .in(RetryTask::getUniqueId, uniqueIds));
        Assert.notEmpty(list, () -> new SnailJobServerException("没有可执行的任务"));

        for (RetryTask retryTask : list) {
            for (TaskExecutor taskExecutor : taskExecutors) {
                if (taskExecutor.getTaskType().getScene() == TaskExecutorSceneEnum.MANUAL_CALLBACK.getScene()) {
                    taskExecutor.actuator(retryTask);
                }
            }

        }

        return true;
    }

}
