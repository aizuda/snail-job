package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.dto.PartitionTask;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.CronUtils;
import com.aizuda.snailjob.server.common.util.PartitionTaskUtils;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.ExportSceneVO;
import com.aizuda.snailjob.server.web.model.request.SceneConfigQueryVO;
import com.aizuda.snailjob.server.web.model.request.SceneConfigRequestVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.response.SceneConfigResponseVO;
import com.aizuda.snailjob.server.web.service.SceneConfigService;
import com.aizuda.snailjob.server.web.service.convert.SceneConfigConverter;
import com.aizuda.snailjob.server.web.service.convert.SceneConfigResponseVOConverter;
import com.aizuda.snailjob.server.web.service.handler.GroupHandler;
import com.aizuda.snailjob.server.web.service.handler.SyncConfigHandler;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.ConfigAccess;
import com.aizuda.snailjob.template.datasource.access.TaskAccess;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetrySummaryMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryDeadLetter;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySummary;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author: opensnail
 * @date : 2022-03-03 10:55
 */
@Service
@RequiredArgsConstructor
@Validated
public class SceneConfigServiceImpl implements SceneConfigService {

    private final AccessTemplate accessTemplate;
    private final GroupHandler groupHandler;
    private final RetrySummaryMapper retrySummaryMapper;



    @Override
    public PageResult<List<SceneConfigResponseVO>> getSceneConfigPageList(SceneConfigQueryVO queryVO) {
        PageDTO<RetrySceneConfig> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        List<String> groupNames = UserSessionUtils.getGroupNames(queryVO.getGroupName());

        pageDTO = accessTemplate.getSceneConfigAccess().listPage(pageDTO,
                new LambdaQueryWrapper<RetrySceneConfig>()
                        .eq(RetrySceneConfig::getNamespaceId, userSessionVO.getNamespaceId())
                        .in(CollUtil.isNotEmpty(groupNames), RetrySceneConfig::getGroupName, groupNames)
                        .eq(Objects.nonNull(queryVO.getSceneStatus()), RetrySceneConfig::getSceneStatus, queryVO.getSceneStatus())
                        .likeRight(StrUtil.isNotBlank(queryVO.getSceneName()), RetrySceneConfig::getSceneName, StrUtil.trim(queryVO.getSceneName()))
                        .orderByDesc(RetrySceneConfig::getCreateDt));

        return new PageResult<>(pageDTO, SceneConfigResponseVOConverter.INSTANCE.convertList(pageDTO.getRecords()));
    }

    @Override
    public List<SceneConfigResponseVO> getSceneConfigList(String groupName) {

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        List<RetrySceneConfig> retrySceneConfigs = accessTemplate.getSceneConfigAccess()
                .list(new LambdaQueryWrapper<RetrySceneConfig>()
                        .eq(RetrySceneConfig::getNamespaceId, namespaceId)
                        .eq(RetrySceneConfig::getGroupName, groupName)
                        .select(RetrySceneConfig::getSceneName,
                                RetrySceneConfig::getDescription, RetrySceneConfig::getMaxRetryCount)
                        .orderByDesc(RetrySceneConfig::getCreateDt));

        return SceneConfigResponseVOConverter.INSTANCE.convertList(retrySceneConfigs);
    }

    @Override
    public Boolean saveSceneConfig(SceneConfigRequestVO requestVO) {

        checkExecuteInterval(requestVO.getBackOff(), requestVO.getTriggerInterval());
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        ConfigAccess<RetrySceneConfig> sceneConfigAccess = accessTemplate.getSceneConfigAccess();
        Assert.isTrue(0 == sceneConfigAccess.count(
                new LambdaQueryWrapper<RetrySceneConfig>()
                        .eq(RetrySceneConfig::getNamespaceId, namespaceId)
                        .eq(RetrySceneConfig::getGroupName, requestVO.getGroupName())
                        .eq(RetrySceneConfig::getSceneName, requestVO.getSceneName())

        ), () -> new SnailJobServerException("Scene name duplicate. {}", requestVO.getSceneName()));

        RetrySceneConfig retrySceneConfig = SceneConfigConverter.INSTANCE.toRetrySceneConfig(requestVO);
        retrySceneConfig.setCreateDt(LocalDateTime.now());
        retrySceneConfig.setNamespaceId(namespaceId);

        if (requestVO.getBackOff() == WaitStrategies.WaitStrategyEnum.DELAY_LEVEL.getType()) {
            retrySceneConfig.setTriggerInterval(StrUtil.EMPTY);
        }

        if (Objects.equals(requestVO.getCbStatus(), StatusEnum.YES.getStatus())) {
            checkExecuteInterval(requestVO.getCbTriggerType(), requestVO.getCbTriggerInterval());
            if (requestVO.getCbTriggerType() == WaitStrategies.WaitStrategyEnum.DELAY_LEVEL.getType()) {
                retrySceneConfig.setCbTriggerInterval(StrUtil.EMPTY);
            }
        }

        Assert.isTrue(1 == sceneConfigAccess.insert(retrySceneConfig),
                () -> new SnailJobServerException("failed to insert scene. retrySceneConfig:[{}]",
                        JsonUtil.toJsonString(retrySceneConfig)));

        // 同步配置到客户端
        SyncConfigHandler.addSyncTask(requestVO.getGroupName(), namespaceId);

        return Boolean.TRUE;
    }

    @Override
    public Boolean updateSceneConfig(SceneConfigRequestVO requestVO) {
        checkExecuteInterval(requestVO.getBackOff(), requestVO.getTriggerInterval());
        RetrySceneConfig retrySceneConfig = SceneConfigConverter.INSTANCE.toRetrySceneConfig(requestVO);
        // 防止更新
        retrySceneConfig.setSceneName(null);
        retrySceneConfig.setGroupName(null);
        retrySceneConfig.setNamespaceId(null);

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        if (Objects.equals(requestVO.getCbStatus(), StatusEnum.YES.getStatus())) {
            checkExecuteInterval(requestVO.getCbTriggerType(), requestVO.getCbTriggerInterval());
            if (requestVO.getCbTriggerType() == WaitStrategies.WaitStrategyEnum.DELAY_LEVEL.getType()) {
                retrySceneConfig.setCbTriggerInterval(StrUtil.EMPTY);
            }
        }

        retrySceneConfig.setTriggerInterval(
                Optional.ofNullable(retrySceneConfig.getTriggerInterval()).orElse(StrUtil.EMPTY));
        Assert.isTrue(1 == accessTemplate.getSceneConfigAccess().update(retrySceneConfig,
                        new LambdaUpdateWrapper<RetrySceneConfig>()
                                .eq(RetrySceneConfig::getNamespaceId, namespaceId)
                                .eq(RetrySceneConfig::getGroupName, requestVO.getGroupName())
                                .eq(RetrySceneConfig::getSceneName, requestVO.getSceneName())),
                () -> new SnailJobServerException("failed to update scene. retrySceneConfig:[{}]",
                        JsonUtil.toJsonString(retrySceneConfig)));



        // 同步配置到客户端
        SyncConfigHandler.addSyncTask(requestVO.getGroupName(), namespaceId);
        return Boolean.TRUE;
    }

    @Override
    public SceneConfigResponseVO getSceneConfigDetail(Long id) {
        RetrySceneConfig retrySceneConfig = accessTemplate.getSceneConfigAccess()
                .one(new LambdaQueryWrapper<RetrySceneConfig>()
                        .eq(RetrySceneConfig::getId, id));
        return SceneConfigResponseVOConverter.INSTANCE.convert(retrySceneConfig);
    }

    @Override
    public boolean updateStatus(final Long id, final Integer status) {

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        RetrySceneConfig config = new RetrySceneConfig();
        config.setSceneStatus(status);

        return 1 == accessTemplate.getSceneConfigAccess().update(config,
                new LambdaUpdateWrapper<RetrySceneConfig>()
                        .eq(RetrySceneConfig::getId, id)
                        .eq(RetrySceneConfig::getNamespaceId, namespaceId));
    }

    @Override
    @Transactional
    public void importSceneConfig(final List<SceneConfigRequestVO> requests) {
        batchSaveSceneConfig(requests, UserSessionUtils.currentUserSession().getNamespaceId());
    }

    @Override
    public String exportSceneConfig(final ExportSceneVO exportSceneVO) {

        List<SceneConfigRequestVO> requestList = new ArrayList<>();

        PartitionTaskUtils.process(startId -> {
            List<RetrySceneConfig> sceneConfigs = accessTemplate.getSceneConfigAccess()
                    .listPage(new PageDTO<>(0, 500), new LambdaQueryWrapper<RetrySceneConfig>()
                            .eq(RetrySceneConfig::getNamespaceId, UserSessionUtils.currentUserSession().getNamespaceId())
                            .eq(Objects.nonNull(exportSceneVO.getSceneStatus()), RetrySceneConfig::getSceneStatus, exportSceneVO.getSceneStatus())
                            .eq(StrUtil.isNotBlank(exportSceneVO.getGroupName()),
                                    RetrySceneConfig::getGroupName, StrUtil.trim(exportSceneVO.getGroupName()))
                            .likeRight(StrUtil.isNotBlank(exportSceneVO.getSceneName()),
                                    RetrySceneConfig::getSceneName, StrUtil.trim(exportSceneVO.getSceneName()))
                            .in(CollUtil.isNotEmpty(exportSceneVO.getSceneIds()), RetrySceneConfig::getId, exportSceneVO.getSceneIds())
                            .ge(RetrySceneConfig::getId, startId)
                            .orderByAsc(RetrySceneConfig::getId)
                    ).getRecords();

            return StreamUtils.toList(sceneConfigs, SceneConfigPartitionTask::new);
        }, partitionTasks -> {
            List<SceneConfigPartitionTask> partitionTaskList = (List<SceneConfigPartitionTask>) partitionTasks;
            requestList.addAll(SceneConfigConverter.INSTANCE.toSceneConfigRequestVOs(
                    StreamUtils.toList(partitionTaskList, SceneConfigPartitionTask::getConfig))
            );
        }, 0);

        return JsonUtil.toJsonString(requestList);
    }

    @Override
    @Transactional
    public boolean deleteByIds(Set<Long> ids) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        LambdaQueryWrapper<RetrySceneConfig> queryWrapper = new LambdaQueryWrapper<RetrySceneConfig>()
                .select(RetrySceneConfig::getSceneName, RetrySceneConfig::getGroupName)
                .eq(RetrySceneConfig::getNamespaceId, namespaceId)
                .eq(RetrySceneConfig::getSceneStatus, StatusEnum.NO.getStatus())
                .in(RetrySceneConfig::getId, ids);

        List<RetrySceneConfig> sceneConfigs = accessTemplate.getSceneConfigAccess().list(queryWrapper);
        Assert.notEmpty(sceneConfigs, () -> new SnailJobServerException("Failed to delete retry scene, please check if the scene status is closed"));

        Set<String> sceneNames = StreamUtils.toSet(sceneConfigs, RetrySceneConfig::getSceneName);
        Set<String> groupNames = StreamUtils.toSet(sceneConfigs, RetrySceneConfig::getGroupName);

        TaskAccess<Retry> retryTaskAccess = accessTemplate.getRetryAccess();
        TaskAccess<RetryDeadLetter> retryTaskTaskAccess = accessTemplate.getRetryDeadLetterAccess();
        for (String groupName : groupNames) {
            List<Retry> retries = retryTaskAccess.listPage(new PageDTO<>(1, 1),
                    new LambdaQueryWrapper<Retry>()
                            .eq(Retry::getGroupName, groupName)
                            .in(Retry::getSceneName, sceneNames)
                            .orderByAsc(Retry::getId)).getRecords();
            Assert.isTrue(CollUtil.isEmpty(retries),
                    () -> new SnailJobServerException("Failed to delete retry scene, retry tasks exist. Please delete the retry tasks before retrying"));

            List<RetryDeadLetter> retryDeadLetters = retryTaskTaskAccess.listPage(new PageDTO<>(1, 1),
                    new LambdaQueryWrapper<RetryDeadLetter>()
                            .eq(RetryDeadLetter::getGroupName, groupName)
                            .in(RetryDeadLetter::getSceneName, sceneNames)
                            .orderByAsc(RetryDeadLetter::getId)).getRecords();
            Assert.isTrue(CollUtil.isEmpty(retryDeadLetters),
                    () -> new SnailJobServerException("Failed to delete retry scene, dead letter tasks exist. Please delete the dead letter tasks before retrying"));
        }

        Assert.isTrue(ids.size() == accessTemplate.getSceneConfigAccess().delete(queryWrapper),
                () -> new SnailJobServerException("Failed to delete retry scene, please check if the scene status is closed"));

        List<RetrySummary> retrySummaries = retrySummaryMapper.selectList(
                new LambdaQueryWrapper<RetrySummary>()
                        .select(RetrySummary::getId)
                        .eq(RetrySummary::getNamespaceId, namespaceId)
                        .in(RetrySummary::getGroupName, groupNames)
                        .in(RetrySummary::getSceneName, sceneNames)
        );

        if (CollUtil.isNotEmpty(retrySummaries)) {
            Assert.isTrue(retrySummaries.size() == retrySummaryMapper.deleteByIds(StreamUtils.toSet(retrySummaries, RetrySummary::getId))
                    , () -> new SnailJobServerException("Failed to delete summary table data"));
        }

        return Boolean.TRUE;
    }

    private void batchSaveSceneConfig(final List<SceneConfigRequestVO> requests, final String namespaceId) {

        Set<String> groupNameSet = Sets.newHashSet();
        Set<String> sceneNameSet = Sets.newHashSet();
        for (final SceneConfigRequestVO requestVO : requests) {
            checkExecuteInterval(requestVO.getBackOff(), requestVO.getTriggerInterval());
            if (Objects.equals(requestVO.getCbStatus(), StatusEnum.YES.getStatus())) {
                checkExecuteInterval(requestVO.getCbTriggerType(), requestVO.getCbTriggerInterval());
            }
            groupNameSet.add(requestVO.getGroupName());
            sceneNameSet.add(requestVO.getSceneName());
        }

        groupHandler.validateGroupExistence(groupNameSet, namespaceId);

        ConfigAccess<RetrySceneConfig> sceneConfigAccess = accessTemplate.getSceneConfigAccess();
        List<RetrySceneConfig> sceneConfigs = sceneConfigAccess.list(
                new LambdaQueryWrapper<RetrySceneConfig>()
                        .select(RetrySceneConfig::getSceneName)
                        .eq(RetrySceneConfig::getNamespaceId, namespaceId)
                        .in(RetrySceneConfig::getGroupName, groupNameSet)
                        .in(RetrySceneConfig::getSceneName, sceneNameSet));

        Assert.isTrue(CollUtil.isEmpty(sceneConfigs), () -> new SnailJobServerException("Import failed. Reason: Scene {} already exists",
                StreamUtils.toSet(sceneConfigs, RetrySceneConfig::getSceneName)));

        LocalDateTime now = LocalDateTime.now();
        List<RetrySceneConfig> retrySceneConfigs = SceneConfigConverter.INSTANCE.toRetrySceneConfigs(requests);
        for (final RetrySceneConfig retrySceneConfig : retrySceneConfigs) {
            retrySceneConfig.setCreateDt(now);
            retrySceneConfig.setNamespaceId(namespaceId);
            if (retrySceneConfig.getBackOff() == WaitStrategies.WaitStrategyEnum.DELAY_LEVEL.getType()) {
                retrySceneConfig.setTriggerInterval(StrUtil.EMPTY);
            }

            if (Objects.equals(retrySceneConfig.getCbStatus(), StatusEnum.YES.getStatus())) {
                if (retrySceneConfig.getCbTriggerType() == WaitStrategies.WaitStrategyEnum.DELAY_LEVEL.getType()) {
                    retrySceneConfig.setCbTriggerInterval(StrUtil.EMPTY);
                }
            }

            Assert.isTrue(1 == sceneConfigAccess.insert(retrySceneConfig),
                    () -> new SnailJobServerException("failed to insert scene. retrySceneConfig:[{}]",
                            JsonUtil.toJsonString(retrySceneConfig)));
        }

    }

    @EqualsAndHashCode(callSuper = true)
    @Getter
    private static class SceneConfigPartitionTask extends PartitionTask {
        // 这里就直接放RetrySceneConfig为了后面若加字段不需要再这里在调整了
        private final RetrySceneConfig config;

        public SceneConfigPartitionTask(@NotNull RetrySceneConfig config) {
            this.config = config;
            setId(config.getId());
        }
    }

    private static void checkExecuteInterval(Integer backOff, String triggerInterval) {
        if (Lists.newArrayList(WaitStrategies.WaitStrategyEnum.FIXED.getType(),
                        WaitStrategies.WaitStrategyEnum.RANDOM.getType()).contains(backOff)) {
            if (Integer.parseInt(triggerInterval) < 10) {
                throw new SnailJobServerException("Interval time must not be less than 10");
            }
        } else if (backOff == WaitStrategies.WaitStrategyEnum.CRON.getType()) {
            if (CronUtils.getExecuteInterval(triggerInterval) < 10 * 1000) {
                throw new SnailJobServerException("Interval time must not be less than 10");
            }
        }
    }

}
