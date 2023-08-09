package com.aizuda.easy.retry.server.support.generator.task;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.enums.DelayLevelEnum;
import com.aizuda.easy.retry.server.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.service.convert.RetryTaskConverter;
import com.aizuda.easy.retry.server.service.convert.RetryTaskLogConverter;
import com.aizuda.easy.retry.server.support.generator.IdGenerator;
import com.aizuda.easy.retry.server.support.generator.TaskGenerator;
import com.aizuda.easy.retry.server.support.strategy.WaitStrategies;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.enums.StatusEnum;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.SceneConfigMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.GroupConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import com.aizuda.easy.retry.template.datasource.utils.RequestDataHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author www.byteblogs.com
 * @date 2023-07-16 11:52:39
 * @since 2.1.0
 */
@Slf4j
public abstract class AbstractGenerator implements TaskGenerator {

    @Autowired
    protected AccessTemplate accessTemplate;
    @Autowired
    private List<IdGenerator> idGeneratorList;
    @Autowired
    private RetryTaskMapper retryTaskMapper;
    @Autowired
    private SceneConfigMapper sceneConfigMapper;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;

    @Override
    @Transactional
    public void taskGenerator(TaskContext taskContext) {
        LogUtils.info(log, "received report data. {}", JsonUtil.toJsonString(taskContext));

        checkAndInitScene(taskContext);

        List<TaskContext.TaskInfo> taskInfos = taskContext.getTaskInfos();

        Set<String> idempotentIdSet = taskInfos.stream().map(TaskContext.TaskInfo::getIdempotentId).collect(Collectors.toSet());

        // 获取相关的任务，用户幂等校验
        RequestDataHelper.setPartition(taskContext.getGroupName());
        List<RetryTask> retryTasks = retryTaskMapper.selectList(new LambdaQueryWrapper<RetryTask>()
                .eq(RetryTask::getGroupName, taskContext.getGroupName())
                .eq(RetryTask::getSceneName, taskContext.getSceneName())
                .in(RetryTask::getIdempotentId, idempotentIdSet));

        Map<String/*幂等ID*/, List<RetryTask>> retryTaskMap = retryTasks.stream().collect(Collectors.groupingBy(RetryTask::getIdempotentId));

        List<RetryTask> waitInsertTasks = new ArrayList<>();
        List<RetryTaskLog> waitInsertTaskLogs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (TaskContext.TaskInfo taskInfo : taskInfos) {
            Pair<List<RetryTask>, List<RetryTaskLog>> pair = doConvertTask(retryTaskMap, taskContext, now, taskInfo);
            waitInsertTasks.addAll(pair.getKey());
            waitInsertTaskLogs.addAll(pair.getValue());
        }

        if (CollectionUtils.isEmpty(waitInsertTasks)) {
            return;
        }

        RequestDataHelper.setPartition(taskContext.getGroupName());
        Assert.isTrue(waitInsertTasks.size() == retryTaskMapper.batchInsert(waitInsertTasks, RequestDataHelper.getPartition()), () -> new EasyRetryServerException("failed to report data"));
        Assert.isTrue(waitInsertTaskLogs.size() == retryTaskLogMapper.batchInsert(waitInsertTaskLogs),
                () -> new EasyRetryServerException("新增重试日志失败"));
    }

    /**
     * @param retryTaskMap
     * @param now
     * @param taskInfo
     */
    private Pair<List<RetryTask>, List<RetryTaskLog>> doConvertTask(Map<String/*幂等ID*/, List<RetryTask>> retryTaskMap,
                                                                    TaskContext taskContext, LocalDateTime now,
                                                                    TaskContext.TaskInfo taskInfo) {
        List<RetryTask> waitInsertTasks = new ArrayList<>();
        List<RetryTaskLog> waitInsertTaskLogs = new ArrayList<>();

        // 判断是否存在与幂等ID相同的任务
        List<RetryTask> list = retryTaskMap.getOrDefault(taskInfo.getIdempotentId(), new ArrayList<>()).stream()
                .filter(retryTask -> taskContext.getGroupName().equals(retryTask.getGroupName())
                        && taskContext.getSceneName().equals(retryTask.getSceneName())).collect(Collectors.toList());
        // 说明存在相同的任务
        if (!CollectionUtils.isEmpty(list)) {
            LogUtils.warn(log, "interrupted reporting in retrying task. [{}]", JsonUtil.toJsonString(taskInfo));
            return Pair.of(waitInsertTasks, waitInsertTaskLogs);
        }

        RetryTask retryTask = RetryTaskConverter.INSTANCE.toRetryTask(taskInfo);
        retryTask.setUniqueId(getIdGenerator(taskContext.getGroupName()));
        retryTask.setTaskType(TaskTypeEnum.RETRY.getType());
        retryTask.setGroupName(taskContext.getGroupName());
        retryTask.setSceneName(taskContext.getSceneName());
        retryTask.setRetryStatus(initStatus(taskContext));
        retryTask.setCreateDt(now);
        retryTask.setUpdateDt(now);

        if (StringUtils.isBlank(retryTask.getExtAttrs())) {
            retryTask.setExtAttrs(StringUtils.EMPTY);
        }

        retryTask.setNextTriggerAt(WaitStrategies.randomWait(1, TimeUnit.SECONDS, 60, TimeUnit.SECONDS).computeRetryTime(null));
        waitInsertTasks.add(retryTask);

        // 初始化日志
        RetryTaskLog retryTaskLog = RetryTaskLogConverter.INSTANCE.toRetryTask(retryTask);
        retryTaskLog.setTaskType(TaskTypeEnum.RETRY.getType());
        retryTaskLog.setCreateDt(now);
        waitInsertTaskLogs.add(retryTaskLog);

        return Pair.of(waitInsertTasks, waitInsertTaskLogs);
    }

    protected abstract Integer initStatus(TaskContext taskContext);

    private void checkAndInitScene( TaskContext taskContext) {
        SceneConfig sceneConfig = accessTemplate.getSceneConfigAccess().getSceneConfigByGroupNameAndSceneName(taskContext.getGroupName(), taskContext.getSceneName());
        if (Objects.isNull(sceneConfig)) {

            GroupConfig groupConfig = accessTemplate.getGroupConfigAccess().getGroupConfigByGroupName(taskContext.getGroupName());
            if (Objects.isNull(groupConfig)) {
                throw new EasyRetryServerException("failed to report data, no group configuration found. groupName:[{}]", taskContext.getGroupName());
            }

            if (groupConfig.getInitScene().equals(StatusEnum.NO.getStatus())) {
                throw new EasyRetryServerException("failed to report data, no scene configuration found. groupName:[{}] sceneName:[{}]", taskContext.getGroupName(), taskContext.getSceneName());
            } else {
                // 若配置了默认初始化场景配置，则发现上报数据的时候未配置场景，默认生成一个场景
                initScene(taskContext.getGroupName(), taskContext.getSceneName());
            }
        }

    }

    /**
     * 若配置了默认初始化场景配置，则发现上报数据的时候未配置场景，默认生成一个场景
     * backOff(退避策略): 等级策略
     * maxRetryCount(最大重试次数): 26
     * triggerInterval(间隔时间): see: {@link DelayLevelEnum}
     *
     * @param groupName 组名称
     * @param sceneName 场景名称
     */
    private void initScene(String groupName, String sceneName) {
        SceneConfig sceneConfig;
        sceneConfig = new SceneConfig();
        sceneConfig.setGroupName(groupName);
        sceneConfig.setSceneName(sceneName);
        sceneConfig.setSceneStatus(StatusEnum.YES.getStatus());
        sceneConfig.setBackOff(WaitStrategies.WaitStrategyEnum.DELAY_LEVEL.getBackOff());
        sceneConfig.setMaxRetryCount(DelayLevelEnum._21.getLevel());
        sceneConfig.setDescription("自动初始化场景");
        Assert.isTrue(1 == sceneConfigMapper.insert(sceneConfig), () -> new EasyRetryServerException("init scene error"));
    }

    /**
     * 获取分布式id
     *
     * @param groupName 组id
     * @return 分布式id
     */
    private String getIdGenerator(String groupName) {

        GroupConfig groupConfig = accessTemplate.getGroupConfigAccess().getGroupConfigByGroupName(groupName);
        for (final IdGenerator idGenerator : idGeneratorList) {
            if (idGenerator.supports(groupConfig.getIdGeneratorMode())) {
                return idGenerator.idGenerator(groupName);
            }
        }

        throw new EasyRetryServerException("id generator mode not configured. [{}]", groupName);
    }
}
