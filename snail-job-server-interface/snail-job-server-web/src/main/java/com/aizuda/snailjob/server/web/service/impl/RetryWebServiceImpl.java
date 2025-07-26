package com.aizuda.snailjob.server.web.service.impl;

import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.dto.InstanceSelectCondition;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import com.aizuda.snailjob.server.service.service.impl.AbstractRetryService;
import lombok.RequiredArgsConstructor;
import org.apache.pekko.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.model.GenerateRetryIdempotentIdDTO;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.enums.RetryTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.enums.TaskGeneratorSceneEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.model.dto.RetryTaskDTO;
import com.aizuda.snailjob.server.retry.task.client.RetryRpcClient;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskPrepareDTO;
import com.aizuda.snailjob.server.retry.task.support.generator.retry.TaskContext;
import com.aizuda.snailjob.server.retry.task.support.generator.retry.TaskGenerator;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.*;
import com.aizuda.snailjob.server.web.model.response.RetryResponseWebVO;
import com.aizuda.snailjob.server.web.service.RetryWebService;
import com.aizuda.snailjob.server.retry.task.convert.RetryConverter;
import com.aizuda.snailjob.server.web.service.convert.RetryTaskResponseVOConverter;
import com.aizuda.snailjob.server.web.service.convert.TaskContextConverter;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.TaskAccess;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.aizuda.snailjob.common.core.enums.RetryStatusEnum.ALLOW_DELETE_STATUS;

/**
 * @author opensnail
 * @date 2022-02-27
 * @since 2.0
 */
@Service("retryWebCommonService")
@RequiredArgsConstructor
public class RetryWebServiceImpl extends AbstractRetryService implements RetryWebService {
    private final RetryTaskMapper retryTaskMapper;
    private final AccessTemplate accessTemplate;
    private final List<TaskGenerator> taskGenerators;
    private final RetryTaskLogMessageMapper retryTaskLogMessageMapper;
    private final TransactionTemplate transactionTemplate;
    private final InstanceManager instanceManager;

    @Override
    public PageResult<List<RetryResponseWebVO>> getRetryPage(RetryQueryVO queryVO) {

        PageDTO<Retry> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        List<String> groupNames = UserSessionUtils.getGroupNames(queryVO.getGroupName());

        LambdaQueryWrapper<Retry> queryWrapper = new LambdaQueryWrapper<Retry>()
                .eq(Retry::getNamespaceId, namespaceId)
                .in(CollUtil.isNotEmpty(groupNames), Retry::getGroupName, groupNames)
                .eq(StrUtil.isNotBlank(queryVO.getSceneName()), Retry::getSceneName, queryVO.getSceneName())
                .eq(StrUtil.isNotBlank(queryVO.getBizNo()), Retry::getBizNo, queryVO.getBizNo())
                .eq(StrUtil.isNotBlank(queryVO.getIdempotentId()), Retry::getIdempotentId, queryVO.getIdempotentId())
                .eq(Objects.nonNull(queryVO.getRetryId()), Retry::getId, queryVO.getRetryId())
                .eq(Objects.nonNull(queryVO.getRetryStatus()), Retry::getRetryStatus, queryVO.getRetryStatus())
                .eq(Retry::getTaskType, SyetemTaskTypeEnum.RETRY.getType())
                .select(Retry::getId, Retry::getBizNo, Retry::getIdempotentId,
                        Retry::getGroupName, Retry::getNextTriggerAt, Retry::getRetryCount,
                        Retry::getRetryStatus, Retry::getUpdateDt, Retry::getCreateDt, Retry::getSceneName,
                        Retry::getTaskType, Retry::getParentId)
                .orderByDesc(Retry::getCreateDt);
        pageDTO = accessTemplate.getRetryAccess().listPage(pageDTO, queryWrapper);

        Set<Long> ids = StreamUtils.toSet(pageDTO.getRecords(), Retry::getId);
        Map<Long, Retry> callbackMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(ids)) {
            List<Retry> callbackTaskList = accessTemplate.getRetryAccess()
                    .list(new LambdaQueryWrapper<Retry>().in(Retry::getParentId, ids));
           callbackMap = StreamUtils.toIdentityMap(callbackTaskList, Retry::getParentId);
        }

        List<RetryResponseWebVO> retryResponseList = RetryTaskResponseVOConverter.INSTANCE.convertList(pageDTO.getRecords());
        for (RetryResponseWebVO retryResponseWebVO : retryResponseList) {
            RetryResponseWebVO responseVO = RetryTaskResponseVOConverter.INSTANCE.convert(callbackMap.get(retryResponseWebVO.getId()));
            if (Objects.isNull(responseVO)) {
                retryResponseWebVO.setChildren(Lists.newArrayList());
            } else {
                retryResponseWebVO.setChildren(Lists.newArrayList(responseVO));
            }
        }

        return new PageResult<>(pageDTO, retryResponseList);
    }

    @Override
    public int saveRetryTask(final RetrySaveRequestVO retryTaskRequestVO) {
        RetryStatusEnum retryStatusEnum = RetryStatusEnum.getByStatus(retryTaskRequestVO.getRetryStatus());
        if (Objects.isNull(retryStatusEnum)) {
            throw new SnailJobServerException("Retry status error");
        }

        TaskGenerator taskGenerator = taskGenerators.stream()
                .filter(t -> t.supports(TaskGeneratorSceneEnum.MANA_SINGLE.getScene()))
                .findFirst().orElseThrow(() -> new SnailJobServerException("No matching task generator found"));
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

        RetrySceneConfig retrySceneConfig = accessTemplate.getSceneConfigAccess()
                .getSceneConfigByGroupNameAndSceneName(generateRetryIdempotentIdVO.getGroupName(),
                        generateRetryIdempotentIdVO.getSceneName(), namespaceId);

        Assert.notNull(retrySceneConfig,
                () -> new SnailJobServerException("Failed to generate idempotentId: RetrySceneConfig nodes exist"));

        InstanceSelectCondition condition = InstanceSelectCondition
                .builder()
                .allocKey(retrySceneConfig.getSceneName())
                .groupName(retrySceneConfig.getGroupName())
                .namespaceId(retrySceneConfig.getNamespaceId())
                .routeKey(retrySceneConfig.getRouteKey())
                .targetLabels(retrySceneConfig.getLabels())
                .build();
        InstanceLiveInfo instance = instanceManager.getALiveInstanceByRouteKey(condition);
        Assert.notNull(instance,
                () -> new SnailJobServerException("Failed to generate idempotentId: No active client nodes exist"));

        // 委托客户端生成idempotentId
        GenerateRetryIdempotentIdDTO generateRetryIdempotentIdDTO = new GenerateRetryIdempotentIdDTO();
        generateRetryIdempotentIdDTO.setGroup(generateRetryIdempotentIdVO.getGroupName());
        generateRetryIdempotentIdDTO.setScene(generateRetryIdempotentIdVO.getSceneName());
        generateRetryIdempotentIdDTO.setArgsStr(generateRetryIdempotentIdVO.getArgsStr());
        generateRetryIdempotentIdDTO.setExecutorName(generateRetryIdempotentIdVO.getExecutorName());
        generateRetryIdempotentIdDTO.setSerializerName(generateRetryIdempotentIdVO.getSerializerName());

        RetryRpcClient rpcClient = RequestBuilder.<RetryRpcClient, Result>newBuilder()
                .nodeInfo(instance)
                .retryTimes(3)
                .failover(false)
                .failRetry(true)
                .retryInterval(1)
                .client(RetryRpcClient.class)
                .build();

        Result result = rpcClient.generateIdempotentId(generateRetryIdempotentIdDTO);

        Assert.notNull(result, () -> new SnailJobServerException("idempotentId generation failed"));
        Assert.isTrue(1 == result.getStatus(),
                () -> new SnailJobServerException("idempotentId generation failed: Ensure that the parameters and executor name are correct"));

        return (String) result.getData();
    }

    @Override
    public int updateRetryExecutorName(final RetryUpdateExecutorNameRequestVO requestVO) {

        Retry retry = new Retry();
        retry.setExecutorName(requestVO.getExecutorName());
        retry.setRetryStatus(requestVO.getRetryStatus());
        retry.setUpdateDt(LocalDateTime.now());

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        // 根据重试数据id，更新执行器名称
        TaskAccess<Retry> retryTaskAccess = accessTemplate.getRetryAccess();
        return retryTaskAccess.update(retry, new LambdaUpdateWrapper<Retry>()
                        .eq(Retry::getNamespaceId, namespaceId)
                        .eq(Retry::getGroupName, requestVO.getGroupName())
                        .in(Retry::getId, requestVO.getIds()));
    }

    @Override
    @Transactional
    public boolean batchDeleteRetry(final BatchDeleteRetryTaskVO requestVO) {
        TaskAccess<Retry> retryTaskAccess = accessTemplate.getRetryAccess();
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        List<Retry> retries = retryTaskAccess.list(new LambdaQueryWrapper<Retry>()
                        .eq(Retry::getNamespaceId, namespaceId)
                        .eq(Retry::getGroupName, requestVO.getGroupName())
                        .in(Retry::getRetryStatus, ALLOW_DELETE_STATUS)
                        .in(Retry::getId, requestVO.getIds())
        );

        Assert.notEmpty(retries,
                () -> new SnailJobServerException("No deletable data, only non-[Processing] data can be deleted"));

        Set<Long> retryIds = StreamUtils.toSet(retries, Retry::getId);
        retryTaskMapper.delete(new LambdaQueryWrapper<RetryTask>()
                .eq(RetryTask::getGroupName, requestVO.getGroupName())
                .eq(RetryTask::getNamespaceId, namespaceId)
                .in(RetryTask::getRetryId, retryIds));

        retryTaskLogMessageMapper.delete(
                new LambdaQueryWrapper<RetryTaskLogMessage>()
                        .eq(RetryTaskLogMessage::getNamespaceId, namespaceId)
                        .eq(RetryTaskLogMessage::getGroupName, requestVO.getGroupName())
                        .in(RetryTaskLogMessage::getRetryId, retryIds));

        Assert.isTrue(requestVO.getIds().size() == retryTaskAccess.delete(new LambdaQueryWrapper<Retry>()
                                .eq(Retry::getNamespaceId, namespaceId)
                                .eq(Retry::getGroupName, requestVO.getGroupName())
                                .in(Retry::getRetryStatus, ALLOW_DELETE_STATUS)
                                .in(Retry::getId, requestVO.getIds()))
                , () -> new SnailJobServerException("Failed to delete retry task, please check if the task status is completed or at maximum attempts"));

        return Boolean.TRUE;
    }

    @Override
    public Integer parseLogs(ParseLogsVO parseLogsVO) {
        RetryStatusEnum retryStatusEnum = RetryStatusEnum.getByStatus(parseLogsVO.getRetryStatus());
        if (Objects.isNull(retryStatusEnum)) {
            throw new SnailJobServerException("Retry status error");
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

        Assert.isFalse(waitInsertList.isEmpty(), () -> new SnailJobServerException("No matching data found"));
        Assert.isTrue(waitInsertList.size() <= 500, () -> new SnailJobServerException("A maximum of 500 data entries can be processed"));

        TaskGenerator taskGenerator = taskGenerators.stream()
                .filter(t -> t.supports(TaskGeneratorSceneEnum.MANA_BATCH.getScene()))
                .findFirst().orElseThrow(() -> new SnailJobServerException("No matching task generator found"));

        boolean allMatch = waitInsertList.stream()
                .allMatch(retryTaskDTO -> retryTaskDTO.getGroupName().equals(parseLogsVO.getGroupName()));
        Assert.isTrue(allMatch, () -> new SnailJobServerException("Data groupName mismatch, please check your data"));

        Map<String, List<RetryTaskDTO>> map = StreamUtils.groupByKey(waitInsertList, RetryTaskDTO::getSceneName);

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        transactionTemplate.execute((status -> {
            map.forEach(((sceneName, retryTaskDTOS) -> {
                TaskContext taskContext = new TaskContext();
                taskContext.setSceneName(sceneName);
                taskContext.setGroupName(parseLogsVO.getGroupName());
                taskContext.setNamespaceId(namespaceId);
                taskContext.setInitStatus(parseLogsVO.getRetryStatus());
                taskContext.setTaskInfos(TaskContextConverter.INSTANCE.convert(retryTaskDTOS));

                // 生成任务
                try {
                    taskGenerator.taskGenerator(taskContext);
                } catch (DuplicateKeyException e) {
                    throw new SnailJobServerException("namespaceId:[{}] groupName:[{}] sceneName:[{}] Task already exists",
                            namespaceId, parseLogsVO.getGroupName(), sceneName);
                }

            }));
            return Boolean.TRUE;
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

        Assert.isTrue(count > 0, () -> new SnailJobServerException("Group [{}] is closed, manual execution is not supported.", requestVO.getGroupName()));

        List<Long> retryIds = requestVO.getRetryIds();

        List<Retry> list = accessTemplate.getRetryAccess().list(new LambdaQueryWrapper<Retry>()
                        .eq(Retry::getNamespaceId, namespaceId)
                        .eq(Retry::getTaskType, SyetemTaskTypeEnum.RETRY.getType())
                        .in(Retry::getId, retryIds)
        );
        Assert.notEmpty(list, () -> new SnailJobServerException("No executable tasks"));

        for (Retry retry : list) {
            RetryTaskPrepareDTO retryTaskPrepareDTO = RetryConverter.INSTANCE.toRetryTaskPrepareDTO(retry);
            // 设置now表示立即执行
            retryTaskPrepareDTO.setNextTriggerAt(DateUtils.toNowMilli());
            retryTaskPrepareDTO.setRetryTaskExecutorScene(RetryTaskExecutorSceneEnum.MANUAL_RETRY.getScene());
            retryTaskPrepareDTO.setRetryId(retry.getId());
            // 准备阶段执行
            ActorRef actorRef = ActorGenerator.retryTaskPrepareActor();
            actorRef.tell(retryTaskPrepareDTO, actorRef);
        }

        return true;
    }

    @Override
    protected String getNamespaceId() {
        return UserSessionUtils.currentUserSession().getNamespaceId();
    }
}
