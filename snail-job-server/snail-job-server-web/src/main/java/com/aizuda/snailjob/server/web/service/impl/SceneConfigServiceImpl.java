package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.CronUtils;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.SceneConfigQueryVO;
import com.aizuda.snailjob.server.web.model.request.SceneConfigRequestVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.response.SceneConfigResponseVO;
import com.aizuda.snailjob.server.web.service.SceneConfigService;
import com.aizuda.snailjob.server.web.service.convert.SceneConfigConverter;
import com.aizuda.snailjob.server.web.service.convert.SceneConfigResponseVOConverter;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.ConfigAccess;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author: opensnail
 * @date : 2022-03-03 10:55
 */
@Service
public class SceneConfigServiceImpl implements SceneConfigService {

    @Autowired
    private AccessTemplate accessTemplate;

    @Override
    public PageResult<List<SceneConfigResponseVO>> getSceneConfigPageList(SceneConfigQueryVO queryVO) {
        PageDTO<RetrySceneConfig> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        String namespaceId = userSessionVO.getNamespaceId();
        LambdaQueryWrapper<RetrySceneConfig> sceneConfigLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sceneConfigLambdaQueryWrapper.eq(RetrySceneConfig::getNamespaceId, namespaceId);

        if (userSessionVO.isUser()) {
            sceneConfigLambdaQueryWrapper.in(RetrySceneConfig::getGroupName, userSessionVO.getGroupNames());
        }

        if (StrUtil.isNotBlank(queryVO.getGroupName())) {
            sceneConfigLambdaQueryWrapper.eq(RetrySceneConfig::getGroupName, queryVO.getGroupName().trim());
        }

        if (StrUtil.isNotBlank(queryVO.getSceneName())) {
            sceneConfigLambdaQueryWrapper.eq(RetrySceneConfig::getSceneName, queryVO.getSceneName().trim());
        }

        pageDTO = accessTemplate.getSceneConfigAccess()
                .listPage(pageDTO, sceneConfigLambdaQueryWrapper.orderByDesc(RetrySceneConfig::getCreateDt));

        return new PageResult<>(pageDTO, SceneConfigResponseVOConverter.INSTANCE.batchConvert(pageDTO.getRecords()));

    }

    @Override
    public List<SceneConfigResponseVO> getSceneConfigList(String groupName) {

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        List<RetrySceneConfig> retrySceneConfigs = accessTemplate.getSceneConfigAccess()
                .list(new LambdaQueryWrapper<RetrySceneConfig>()
                        .select(RetrySceneConfig::getSceneName, RetrySceneConfig::getDescription, RetrySceneConfig::getMaxRetryCount)
                        .eq(RetrySceneConfig::getNamespaceId, namespaceId)
                        .eq(RetrySceneConfig::getGroupName, groupName)
                        .orderByDesc(RetrySceneConfig::getCreateDt));

        return SceneConfigResponseVOConverter.INSTANCE.batchConvert(retrySceneConfigs);
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

        RetrySceneConfig retrySceneConfig = SceneConfigConverter.INSTANCE.toSceneConfigRequestVO(requestVO);
        retrySceneConfig.setCreateDt(LocalDateTime.now());
        retrySceneConfig.setNamespaceId(namespaceId);
        Assert.isTrue(1 == sceneConfigAccess.insert(retrySceneConfig),
                () -> new SnailJobServerException("failed to insert scene. retrySceneConfig:[{}]",
                        JsonUtil.toJsonString(retrySceneConfig)));
        return Boolean.TRUE;
    }

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
    public Boolean updateSceneConfig(SceneConfigRequestVO requestVO) {
        checkExecuteInterval(requestVO);
        RetrySceneConfig retrySceneConfig = SceneConfigConverter.INSTANCE.toSceneConfigRequestVO(requestVO);
        // 防止更新
        retrySceneConfig.setSceneName(null);
        retrySceneConfig.setGroupName(null);
        retrySceneConfig.setNamespaceId(null);

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        retrySceneConfig.setTriggerInterval(Optional.ofNullable(retrySceneConfig.getTriggerInterval()).orElse(StrUtil.EMPTY));
        Assert.isTrue(1 == accessTemplate.getSceneConfigAccess().update(retrySceneConfig,
                        new LambdaUpdateWrapper<RetrySceneConfig>()
                                .eq(RetrySceneConfig::getNamespaceId, namespaceId)
                                .eq(RetrySceneConfig::getGroupName, requestVO.getGroupName())
                                .eq(RetrySceneConfig::getSceneName, requestVO.getSceneName())),
                () -> new SnailJobServerException("failed to update scene. retrySceneConfig:[{}]",
                        JsonUtil.toJsonString(retrySceneConfig)));
        return Boolean.TRUE;
    }

    @Override
    public SceneConfigResponseVO getSceneConfigDetail(Long id) {
        RetrySceneConfig retrySceneConfig = accessTemplate.getSceneConfigAccess().one(new LambdaQueryWrapper<RetrySceneConfig>()
                .eq(RetrySceneConfig::getId, id));
        return SceneConfigResponseVOConverter.INSTANCE.convert(retrySceneConfig);
    }
}
