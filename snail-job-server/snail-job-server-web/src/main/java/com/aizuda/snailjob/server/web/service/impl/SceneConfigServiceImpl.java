package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.CronUtils;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.SceneConfigQueryVO;
import com.aizuda.snailjob.server.web.model.request.SceneConfigRequestVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.response.SceneConfigResponseVO;
import com.aizuda.snailjob.server.web.service.SceneConfigService;
import com.aizuda.snailjob.server.web.service.convert.SceneConfigConverter;
import com.aizuda.snailjob.server.web.service.convert.SceneConfigResponseVOConverter;
import com.aizuda.snailjob.server.web.service.handler.SyncConfigHandler;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.ConfigAccess;
import com.aizuda.snailjob.template.datasource.persistence.mapper.NamespaceMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.Namespace;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2022-03-03 10:55
 */
@Service
@RequiredArgsConstructor
public class SceneConfigServiceImpl implements SceneConfigService {

    private final AccessTemplate accessTemplate;
    private final NamespaceMapper namespaceMapper;

    private static void checkExecuteInterval(SceneConfigRequestVO requestVO) {
        if (Lists.newArrayList(WaitStrategies.WaitStrategyEnum.FIXED.getType(),
                WaitStrategies.WaitStrategyEnum.RANDOM.getType())
            .contains(requestVO.getBackOff())) {
            if (Integer.parseInt(requestVO.getTriggerInterval()) < 10) {
                throw new SnailJobServerException("间隔时间不得小于10");
            }
        } else if (requestVO.getBackOff() == WaitStrategies.WaitStrategyEnum.CRON.getType()) {
            if (CronUtils.getExecuteInterval(requestVO.getTriggerInterval()) < 10 * 1000) {
                throw new SnailJobServerException("间隔时间不得小于10");
            }
        }
    }

    @Override
    public PageResult<List<SceneConfigResponseVO>> getSceneConfigPageList(SceneConfigQueryVO queryVO) {
        PageDTO<RetrySceneConfig> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        String namespaceId = userSessionVO.getNamespaceId();
        pageDTO = accessTemplate.getSceneConfigAccess().listPage(pageDTO,
            new LambdaQueryWrapper<RetrySceneConfig>()
                .eq(RetrySceneConfig::getNamespaceId, namespaceId)
                .in(userSessionVO.isUser(), RetrySceneConfig::getGroupName, userSessionVO.getGroupNames())
                .eq(StrUtil.isNotBlank(queryVO.getGroupName()),
                    RetrySceneConfig::getGroupName, StrUtil.trim(queryVO.getGroupName()))
                .eq(StrUtil.isNotBlank(queryVO.getSceneName()),
                    RetrySceneConfig::getSceneName, StrUtil.trim(queryVO.getSceneName()))
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

        checkExecuteInterval(requestVO);
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        ConfigAccess<RetrySceneConfig> sceneConfigAccess = accessTemplate.getSceneConfigAccess();
        Assert.isTrue(0 == sceneConfigAccess.count(
            new LambdaQueryWrapper<RetrySceneConfig>()
                .eq(RetrySceneConfig::getNamespaceId, namespaceId)
                .eq(RetrySceneConfig::getGroupName, requestVO.getGroupName())
                .eq(RetrySceneConfig::getSceneName, requestVO.getSceneName())

        ), () -> new SnailJobServerException("场景名称重复. {}", requestVO.getSceneName()));

        RetrySceneConfig retrySceneConfig = SceneConfigConverter.INSTANCE.toRetrySceneConfig(requestVO);
        retrySceneConfig.setCreateDt(LocalDateTime.now());
        retrySceneConfig.setNamespaceId(namespaceId);
        if (requestVO.getBackOff() == WaitStrategies.WaitStrategyEnum.DELAY_LEVEL.getType()) {
            retrySceneConfig.setTriggerInterval(StrUtil.EMPTY);
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
        checkExecuteInterval(requestVO);
        RetrySceneConfig retrySceneConfig = SceneConfigConverter.INSTANCE.toRetrySceneConfig(requestVO);
        // 防止更新
        retrySceneConfig.setSceneName(null);
        retrySceneConfig.setGroupName(null);
        retrySceneConfig.setNamespaceId(null);

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

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
    public String exportSceneConfig(final Set<Long> sceneIds) {
        List<RetrySceneConfig> sceneConfigs = accessTemplate.getSceneConfigAccess()
            .list(new LambdaQueryWrapper<RetrySceneConfig>()
                .eq(RetrySceneConfig::getNamespaceId, UserSessionUtils.currentUserSession().getNamespaceId())
                .in(RetrySceneConfig::getId, sceneIds)
            );

        List<SceneConfigRequestVO> sceneConfigRequestVOs = SceneConfigConverter.INSTANCE.toSceneConfigRequestVOs(
            sceneConfigs);
        return JsonUtil.toJsonString(sceneConfigRequestVOs);
    }

    @Override
    @Transactional
    public void batchCopy(final Long targetNamespaceId, final Set<Long> sceneIds) {
        Assert.notEmpty(sceneIds, () -> new SnailJobServerException("参数错误"));

        Namespace namespace = namespaceMapper.selectById(targetNamespaceId);
        Assert.notNull(namespace, () -> new SnailJobServerException("空间不存在"));

        List<RetrySceneConfig> sceneConfigs = accessTemplate.getSceneConfigAccess()
            .list(new LambdaQueryWrapper<RetrySceneConfig>()
                .eq(RetrySceneConfig::getNamespaceId, UserSessionUtils.currentUserSession().getNamespaceId())
                .in(RetrySceneConfig::getId, sceneIds)
            );

        Assert.isTrue(sceneIds.size() == sceneConfigs.size(), () -> new SnailJobServerException("存在部分场景配置不存在"));

        List<SceneConfigRequestVO> sceneConfigRequestVOs = SceneConfigConverter.INSTANCE.toSceneConfigRequestVOs(
            sceneConfigs);
        // 批量写入
        batchSaveSceneConfig(sceneConfigRequestVOs, namespace.getUniqueId());
    }

    private void batchSaveSceneConfig(final List<SceneConfigRequestVO> requests, final String namespaceId) {

        Set<String> groupNameSet = Sets.newHashSet();
        Set<String> sceneNameSet = Sets.newHashSet();
        for (final SceneConfigRequestVO request : requests) {
            checkExecuteInterval(request);
            groupNameSet.add(request.getGroupName());
            sceneNameSet.add(request.getSceneName());
        }

        List<GroupConfig> groupConfigs = accessTemplate.getGroupConfigAccess()
            .list(new LambdaQueryWrapper<GroupConfig>()
                .select(GroupConfig::getGroupName)
                .eq(GroupConfig::getNamespaceId, namespaceId)
                .in(GroupConfig::getGroupName, groupNameSet)
            );

        SetView<String> notExistedGroupNameSet = Sets.difference(groupNameSet,
            StreamUtils.toSet(groupConfigs, GroupConfig::getGroupName));

        Assert.notEmpty(notExistedGroupNameSet,
            () -> new SnailJobServerException("组:{}不存在", notExistedGroupNameSet));

        ConfigAccess<RetrySceneConfig> sceneConfigAccess = accessTemplate.getSceneConfigAccess();
        List<RetrySceneConfig> sceneConfigs = sceneConfigAccess.list(
            new LambdaQueryWrapper<RetrySceneConfig>()
                .select(RetrySceneConfig::getSceneName)
                .eq(RetrySceneConfig::getNamespaceId, namespaceId)
                .in(RetrySceneConfig::getGroupName, groupNameSet)
                .eq(RetrySceneConfig::getSceneName, sceneNameSet));

        Assert.isTrue(CollUtil.isEmpty(sceneConfigs), () -> new SnailJobServerException("场景:{}已存在",
            StreamUtils.toSet(sceneConfigs, RetrySceneConfig::getSceneName)));

        LocalDateTime now = LocalDateTime.now();
        List<RetrySceneConfig> retrySceneConfigs = SceneConfigConverter.INSTANCE.toRetrySceneConfigs(requests);
        for (final RetrySceneConfig retrySceneConfig : retrySceneConfigs) {
            retrySceneConfig.setCreateDt(now);
            retrySceneConfig.setNamespaceId(namespaceId);
            if (retrySceneConfig.getBackOff() == WaitStrategies.WaitStrategyEnum.DELAY_LEVEL.getType()) {
                retrySceneConfig.setTriggerInterval(StrUtil.EMPTY);
            }

            // TODO 优化成批量插入
            Assert.isTrue(1 == sceneConfigAccess.insert(retrySceneConfig),
                () -> new SnailJobServerException("failed to insert scene. retrySceneConfig:[{}]",
                    JsonUtil.toJsonString(retrySceneConfig)));
        }

    }
}
