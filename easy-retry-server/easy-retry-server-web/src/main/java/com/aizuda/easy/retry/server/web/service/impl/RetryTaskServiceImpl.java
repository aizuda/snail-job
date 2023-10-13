package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.client.model.GenerateRetryIdempotentIdDTO;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.common.client.RequestBuilder;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.common.enums.TaskGeneratorScene;
import com.aizuda.easy.retry.server.common.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import com.aizuda.easy.retry.server.retry.task.client.RetryRpcClient;
import com.aizuda.easy.retry.server.retry.task.generator.task.TaskContext;
import com.aizuda.easy.retry.server.retry.task.generator.task.TaskGenerator;
import com.aizuda.easy.retry.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.task.TaskExecutor;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.task.TaskExecutorSceneEnum;
import com.aizuda.easy.retry.server.retry.task.support.strategy.WaitStrategies;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.BatchDeleteRetryTaskVO;
import com.aizuda.easy.retry.server.web.model.request.GenerateRetryIdempotentIdVO;
import com.aizuda.easy.retry.server.web.model.request.ManualTriggerTaskRequestVO;
import com.aizuda.easy.retry.server.web.model.request.ParseLogsVO;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskQueryVO;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskSaveRequestVO;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskUpdateExecutorNameRequestVO;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskUpdateStatusRequestVO;
import com.aizuda.easy.retry.server.web.model.response.RetryTaskResponseVO;
import com.aizuda.easy.retry.server.web.service.RetryTaskService;
import com.aizuda.easy.retry.server.web.service.convert.RetryTaskResponseVOConverter;
import com.aizuda.easy.retry.server.web.service.convert.TaskContextConverter;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.access.TaskAccess;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLogMessage;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import com.aizuda.easy.retry.template.datasource.utils.RequestDataHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author www.byteblogs.com
 * @date 2022-02-27
 * @since 2.0
 */
@Service
public class RetryTaskServiceImpl implements RetryTaskService {

    public static final String URL = "http://{0}:{1}/{2}/retry/generate/idempotent-id/v1";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ClientNodeAllocateHandler clientNodeAllocateHandler;
    @Autowired
    private RetryTaskLogMessageMapper retryTaskLogMessageMapper;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;
    @Autowired
    private AccessTemplate accessTemplate;
    @Autowired
    private List<TaskGenerator> taskGenerators;
    @Autowired
    private List<TaskExecutor> taskExecutors;

    @Override
    public PageResult<List<RetryTaskResponseVO>> getRetryTaskPage(RetryTaskQueryVO queryVO) {

        PageDTO<RetryTask> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        LambdaQueryWrapper<RetryTask> retryTaskLambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(queryVO.getGroupName())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getGroupName, queryVO.getGroupName());
        } else {
            return new PageResult<>(pageDTO, new ArrayList<>());
        }

        if (StrUtil.isNotBlank(queryVO.getSceneName())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getSceneName, queryVO.getSceneName());
        }
        if (StrUtil.isNotBlank(queryVO.getBizNo())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getBizNo, queryVO.getBizNo());
        }
        if (StrUtil.isNotBlank(queryVO.getIdempotentId())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getIdempotentId, queryVO.getIdempotentId());
        }
        if (StrUtil.isNotBlank(queryVO.getUniqueId())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getUniqueId, queryVO.getUniqueId());
        }
        if (Objects.nonNull(queryVO.getRetryStatus())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getRetryStatus, queryVO.getRetryStatus());
        }

        RequestDataHelper.setPartition(queryVO.getGroupName());

        retryTaskLambdaQueryWrapper.select(RetryTask::getId, RetryTask::getBizNo, RetryTask::getIdempotentId,
                RetryTask::getGroupName, RetryTask::getNextTriggerAt, RetryTask::getRetryCount,
                RetryTask::getRetryStatus, RetryTask::getUpdateDt, RetryTask::getSceneName, RetryTask::getUniqueId, RetryTask::getTaskType);
        pageDTO = accessTemplate.getRetryTaskAccess().listPage(queryVO.getGroupName(), pageDTO,
                retryTaskLambdaQueryWrapper.orderByDesc(RetryTask::getCreateDt));
        return new PageResult<>(pageDTO, RetryTaskResponseVOConverter.INSTANCE.toRetryTaskResponseVO(pageDTO.getRecords()));
    }

    @Override
    public RetryTaskResponseVO getRetryTaskById(String groupName, Long id) {
        RequestDataHelper.setPartition(groupName);
        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        RetryTask retryTask = retryTaskAccess.one(groupName, new LambdaQueryWrapper<RetryTask>().eq(RetryTask::getId, id));
        return RetryTaskResponseVOConverter.INSTANCE.toRetryTaskResponseVO(retryTask);
    }

    @Override
    @Transactional
    public int updateRetryTaskStatus(RetryTaskUpdateStatusRequestVO retryTaskUpdateStatusRequestVO) {

        RetryStatusEnum retryStatusEnum = RetryStatusEnum.getByStatus(retryTaskUpdateStatusRequestVO.getRetryStatus());
        if (Objects.isNull(retryStatusEnum)) {
            throw new EasyRetryServerException("重试状态错误");
        }

        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        RetryTask retryTask = retryTaskAccess.one(retryTaskUpdateStatusRequestVO.getGroupName(),
                new LambdaQueryWrapper<RetryTask>().eq(RetryTask::getId, retryTaskUpdateStatusRequestVO.getId()));
        if (Objects.isNull(retryTask)) {
            throw new EasyRetryServerException("未查询到重试任务");
        }

        retryTask.setRetryStatus(retryTaskUpdateStatusRequestVO.getRetryStatus());
        retryTask.setGroupName(retryTaskUpdateStatusRequestVO.getGroupName());

        // 若恢复重试则需要重新计算下次触发时间
        if (RetryStatusEnum.RUNNING.getStatus().equals(retryStatusEnum.getStatus())) {
            retryTask.setNextTriggerAt(
                    WaitStrategies.randomWait(1, TimeUnit.SECONDS, 60, TimeUnit.SECONDS).computeRetryTime(null));
        }

        if (RetryStatusEnum.FINISH.getStatus().equals(retryStatusEnum.getStatus())) {

            RetryTaskLogMessage retryTaskLogMessage = new RetryTaskLogMessage();
            retryTaskLogMessage.setUniqueId(retryTask.getUniqueId());
            retryTaskLogMessage.setGroupName(retryTask.getGroupName());
            retryTaskLogMessage.setMessage("页面操作完成");
            retryTaskLogMessage.setCreateDt(LocalDateTime.now());
            retryTaskLogMessageMapper.insert(retryTaskLogMessage);

            RetryTaskLog retryTaskLog = new RetryTaskLog();
            retryTaskLog.setRetryStatus(RetryStatusEnum.FINISH.getStatus());
            retryTaskLogMapper.update(retryTaskLog, new LambdaUpdateWrapper<RetryTaskLog>()
                    .eq(RetryTaskLog::getUniqueId, retryTask.getUniqueId())
                    .eq(RetryTaskLog::getGroupName, retryTask.getGroupName()));
        }

        retryTask.setUpdateDt(LocalDateTime.now());
        return retryTaskAccess.updateById(retryTaskUpdateStatusRequestVO.getGroupName(), retryTask);
    }

    @Override
    public int saveRetryTask(final RetryTaskSaveRequestVO retryTaskRequestVO) {
        RetryStatusEnum retryStatusEnum = RetryStatusEnum.getByStatus(retryTaskRequestVO.getRetryStatus());
        if (Objects.isNull(retryStatusEnum)) {
            throw new EasyRetryServerException("重试状态错误");
        }

        TaskGenerator taskGenerator = taskGenerators.stream()
                .filter(t -> t.supports(TaskGeneratorScene.MANA_SINGLE.getScene()))
                .findFirst().orElseThrow(() -> new EasyRetryServerException("没有匹配的任务生成器"));

        TaskContext taskContext = new TaskContext();
        taskContext.setSceneName(retryTaskRequestVO.getSceneName());
        taskContext.setGroupName(retryTaskRequestVO.getGroupName());
        taskContext.setInitStatus(retryTaskRequestVO.getRetryStatus());
        taskContext.setTaskInfos(Collections.singletonList(TaskContextConverter.INSTANCE.toTaskContextInfo(retryTaskRequestVO)));

        // 生成任务
        taskGenerator.taskGenerator(taskContext);

        return 1;
    }

    @Override
    public String idempotentIdGenerate(final GenerateRetryIdempotentIdVO generateRetryIdempotentIdVO) {

        Set<RegisterNodeInfo> serverNodes = CacheRegisterTable.getServerNodeSet(generateRetryIdempotentIdVO.getGroupName());
        Assert.notEmpty(serverNodes, () -> new EasyRetryServerException("生成idempotentId失败: 不存在活跃的客户端节点"));

        SceneConfig sceneConfig = accessTemplate.getSceneConfigAccess()
            .getSceneConfigByGroupNameAndSceneName(generateRetryIdempotentIdVO.getGroupName(),
                generateRetryIdempotentIdVO.getSceneName());

        RegisterNodeInfo serverNode = clientNodeAllocateHandler.getServerNode(sceneConfig.getSceneName(),
            sceneConfig.getGroupName(), sceneConfig.getRouteKey());

        // 委托客户端生成idempotentId
        GenerateRetryIdempotentIdDTO generateRetryIdempotentIdDTO = new GenerateRetryIdempotentIdDTO();
        generateRetryIdempotentIdDTO.setGroup(generateRetryIdempotentIdVO.getGroupName());
        generateRetryIdempotentIdDTO.setScene(generateRetryIdempotentIdVO.getSceneName());
        generateRetryIdempotentIdDTO.setArgsStr(generateRetryIdempotentIdVO.getArgsStr());
        generateRetryIdempotentIdDTO.setExecutorName(generateRetryIdempotentIdVO.getExecutorName());

        RetryRpcClient rpcClient = RequestBuilder.<RetryRpcClient, Result>newBuilder()
            .hostPort(serverNode.getHostPort())
            .groupName(serverNode.getGroupName())
            .hostId(serverNode.getHostId())
            .hostIp(serverNode.getHostIp())
            .contextPath(serverNode.getContextPath())
            .client(RetryRpcClient.class)
            .build();

        Result result = rpcClient.generateIdempotentId(generateRetryIdempotentIdDTO);

        Assert.notNull(result, () -> new EasyRetryServerException("idempotentId生成失败"));
        Assert.isTrue(1 == result.getStatus(), () -> new EasyRetryServerException("idempotentId生成失败:请确保参数与执行器名称正确"));

        return (String) result.getData();
    }

    @Override
    public int updateRetryTaskExecutorName(final RetryTaskUpdateExecutorNameRequestVO requestVO) {

        RetryTask retryTask = new RetryTask();
        retryTask.setExecutorName(requestVO.getExecutorName());
        retryTask.setRetryStatus(requestVO.getRetryStatus());
        retryTask.setUpdateDt(LocalDateTime.now());

        // 根据重试数据id，更新执行器名称
        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        return retryTaskAccess.update(requestVO.getGroupName(), retryTask,
                new LambdaUpdateWrapper<RetryTask>()
                        .eq(RetryTask::getGroupName, requestVO.getGroupName())
                        .in(RetryTask::getId, requestVO.getIds()));
    }

    @Override
    public Integer deleteRetryTask(final BatchDeleteRetryTaskVO requestVO) {
        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        return retryTaskAccess.delete(requestVO.getGroupName(),
                new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getGroupName, requestVO.getGroupName())
                        .in(RetryTask::getId, requestVO.getIds()));
    }

    @Override
    public Integer parseLogs(ParseLogsVO parseLogsVO) {
        RetryStatusEnum retryStatusEnum = RetryStatusEnum.getByStatus(parseLogsVO.getRetryStatus());
        if (Objects.isNull(retryStatusEnum)) {
            throw new EasyRetryServerException("重试状态错误");
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
            if (!CollectionUtils.isEmpty(retryTaskList)) {
                waitInsertList.addAll(retryTaskList);
            }
        }

        Assert.isFalse(waitInsertList.isEmpty(), () -> new EasyRetryServerException("未找到匹配的数据"));
        Assert.isTrue(waitInsertList.size() <= 500, () -> new EasyRetryServerException("最多只能处理500条数据"));

        TaskGenerator taskGenerator = taskGenerators.stream()
                .filter(t -> t.supports(TaskGeneratorScene.MANA_BATCH.getScene()))
                .findFirst().orElseThrow(() -> new EasyRetryServerException("没有匹配的任务生成器"));

        boolean allMatch = waitInsertList.stream().allMatch(retryTaskDTO -> retryTaskDTO.getGroupName().equals(parseLogsVO.getGroupName()));
        Assert.isTrue(allMatch, () -> new EasyRetryServerException("存在数据groupName不匹配，请检查您的数据"));

        Map<String, List<RetryTaskDTO>> map = waitInsertList.stream().collect(Collectors.groupingBy(RetryTaskDTO::getSceneName));

        map.forEach(((sceneName, retryTaskDTOS) -> {
            TaskContext taskContext = new TaskContext();
            taskContext.setSceneName(sceneName);
            taskContext.setGroupName(parseLogsVO.getGroupName());
            taskContext.setInitStatus(parseLogsVO.getRetryStatus());
            taskContext.setTaskInfos(TaskContextConverter.INSTANCE.toTaskContextInfo(retryTaskDTOS));

            // 生成任务
            taskGenerator.taskGenerator(taskContext);
        }));

        return waitInsertList.size();
    }

    @Override
    public boolean manualTriggerRetryTask(ManualTriggerTaskRequestVO requestVO) {

        List<String> uniqueIds = requestVO.getUniqueIds();

        List<RetryTask> list = accessTemplate.getRetryTaskAccess().list(requestVO.getGroupName(),
                new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getTaskType, TaskTypeEnum.RETRY.getType())
                        .in(RetryTask::getUniqueId, uniqueIds));
        Assert.notEmpty(list, () -> new EasyRetryServerException("没有可执行的任务"));


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

        List<RetryTask> list = accessTemplate.getRetryTaskAccess().list(requestVO.getGroupName(),
                new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getTaskType, TaskTypeEnum.CALLBACK.getType())
                        .in(RetryTask::getUniqueId, uniqueIds));
        Assert.notEmpty(list, () -> new EasyRetryServerException("没有可执行的任务"));

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
