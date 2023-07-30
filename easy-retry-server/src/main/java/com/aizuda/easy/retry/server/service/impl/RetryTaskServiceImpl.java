package com.aizuda.easy.retry.server.service.impl;

import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.client.model.GenerateRetryIdempotentIdDTO;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.akka.ActorGenerator;
import com.aizuda.easy.retry.server.config.RequestDataHelper;
import com.aizuda.easy.retry.server.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.enums.TaskGeneratorScene;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskLogMessageMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.GroupConfig;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTaskLog;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTaskLogMessage;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import com.aizuda.easy.retry.server.service.RetryTaskService;
import com.aizuda.easy.retry.server.service.convert.RetryTaskConverter;
import com.aizuda.easy.retry.server.service.convert.RetryTaskResponseVOConverter;
import com.aizuda.easy.retry.server.service.convert.TaskContextConverter;
import com.aizuda.easy.retry.server.support.dispatch.actor.log.RetryTaskLogDTO;
import com.aizuda.easy.retry.server.support.generator.IdGenerator;
import com.aizuda.easy.retry.server.support.generator.TaskGenerator;
import com.aizuda.easy.retry.server.support.generator.task.TaskContext;
import com.aizuda.easy.retry.server.support.handler.ClientNodeAllocateHandler;
import com.aizuda.easy.retry.server.support.strategy.WaitStrategies;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.*;
import com.aizuda.easy.retry.server.web.model.response.RetryTaskResponseVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
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
    private RetryTaskMapper retryTaskMapper;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ClientNodeAllocateHandler clientNodeAllocateHandler;
    @Autowired
    private List<IdGenerator> idGeneratorList;
    @Autowired
    private RetryTaskLogMessageMapper retryTaskLogMessageMapper;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;
    @Autowired
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;
    @Autowired
    private List<TaskGenerator> taskGenerators;

    @Override
    public PageResult<List<RetryTaskResponseVO>> getRetryTaskPage(RetryTaskQueryVO queryVO) {

        PageDTO<RetryTask> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        LambdaQueryWrapper<RetryTask> retryTaskLambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(queryVO.getGroupName())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getGroupName, queryVO.getGroupName());
        } else {
            return new PageResult<>(pageDTO, new ArrayList<>());
        }

        if (StringUtils.isNotBlank(queryVO.getSceneName())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getSceneName, queryVO.getSceneName());
        }
        if (StringUtils.isNotBlank(queryVO.getBizNo())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getBizNo, queryVO.getBizNo());
        }
        if (StringUtils.isNotBlank(queryVO.getIdempotentId())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getIdempotentId, queryVO.getIdempotentId());
        }
        if (StringUtils.isNotBlank(queryVO.getUniqueId())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getUniqueId, queryVO.getUniqueId());
        }
        if (Objects.nonNull(queryVO.getRetryStatus())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getRetryStatus, queryVO.getRetryStatus());
        }

        RequestDataHelper.setPartition(queryVO.getGroupName());

        retryTaskLambdaQueryWrapper.select(RetryTask::getId, RetryTask::getBizNo, RetryTask::getIdempotentId,
                RetryTask::getGroupName, RetryTask::getNextTriggerAt, RetryTask::getRetryCount,
                RetryTask::getRetryStatus, RetryTask::getUpdateDt, RetryTask::getSceneName, RetryTask::getUniqueId, RetryTask::getTaskType);
        pageDTO = retryTaskMapper.selectPage(pageDTO, retryTaskLambdaQueryWrapper.orderByDesc(RetryTask::getCreateDt));
        return new PageResult<>(pageDTO, RetryTaskResponseVOConverter.INSTANCE.toRetryTaskResponseVO(pageDTO.getRecords()));
    }

    @Override
    public RetryTaskResponseVO getRetryTaskById(String groupName, Long id) {
        RequestDataHelper.setPartition(groupName);
        return RetryTaskResponseVOConverter.INSTANCE.toRetryTaskResponseVO(retryTaskMapper.selectById(id));
    }

    @Override
    @Transactional
    public int updateRetryTaskStatus(RetryTaskUpdateStatusRequestVO retryTaskUpdateStatusRequestVO) {

        RetryStatusEnum retryStatusEnum = RetryStatusEnum.getByStatus(retryTaskUpdateStatusRequestVO.getRetryStatus());
        if (Objects.isNull(retryStatusEnum)) {
            throw new EasyRetryServerException("重试状态错误");
        }

        RequestDataHelper.setPartition(retryTaskUpdateStatusRequestVO.getGroupName());
        RetryTask retryTask = retryTaskMapper.selectById(retryTaskUpdateStatusRequestVO.getId());
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

        RequestDataHelper.setPartition(retryTaskUpdateStatusRequestVO.getGroupName());
        return retryTaskMapper.updateById(retryTask);
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
        RegisterNodeInfo serverNode = clientNodeAllocateHandler.getServerNode(generateRetryIdempotentIdVO.getGroupName());
        Assert.notNull(serverNode, () -> new EasyRetryServerException("生成idempotentId失败: 不存在活跃的客户端节点"));

        // 委托客户端生成idempotentId
        String url = MessageFormat
                .format(URL, serverNode.getHostIp(), serverNode.getHostPort().toString(), serverNode.getContextPath());

        GenerateRetryIdempotentIdDTO generateRetryIdempotentIdDTO = new GenerateRetryIdempotentIdDTO();
        generateRetryIdempotentIdDTO.setGroup(generateRetryIdempotentIdVO.getGroupName());
        generateRetryIdempotentIdDTO.setScene(generateRetryIdempotentIdVO.getSceneName());
        generateRetryIdempotentIdDTO.setArgsStr(generateRetryIdempotentIdVO.getArgsStr());
        generateRetryIdempotentIdDTO.setExecutorName(generateRetryIdempotentIdVO.getExecutorName());

        HttpEntity<GenerateRetryIdempotentIdDTO> requestEntity = new HttpEntity<>(generateRetryIdempotentIdDTO);
        Result result = restTemplate.postForObject(url, requestEntity, Result.class);

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
        RequestDataHelper.setPartition(requestVO.getGroupName());
        return retryTaskMapper
                .update(retryTask, new LambdaUpdateWrapper<RetryTask>().in(RetryTask::getId, requestVO.getIds()));
    }

    @Override
    public Integer deleteRetryTask(final BatchDeleteRetryTaskVO requestVO) {
        RequestDataHelper.setPartition(requestVO.getGroupName());
        return retryTaskMapper.deleteBatchIds(requestVO.getIds());
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
            if (StringUtils.isBlank(extractedData)) {
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

    /**
     * 获取分布式id
     *
     * @param groupName 组id
     * @return 分布式id
     */
    private String getIdGenerator(String groupName) {

        GroupConfig groupConfig = configAccess.getGroupConfigByGroupName(groupName);
        for (final IdGenerator idGenerator : idGeneratorList) {
            if (idGenerator.supports(groupConfig.getIdGeneratorMode())) {
                return idGenerator.idGenerator(groupName);
            }
        }

        throw new EasyRetryServerException("id generator mode not configured. [{}]", groupName);
    }
}
