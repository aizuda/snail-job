package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.NotifyConfigQueryVO;
import com.aizuda.snailjob.server.web.model.request.NotifyConfigRequestVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.response.NotifyConfigResponseVO;
import com.aizuda.snailjob.server.web.service.NotifyConfigService;
import com.aizuda.snailjob.server.web.service.convert.NotifyConfigConverter;
import com.aizuda.snailjob.server.web.service.convert.NotifyConfigResponseVOConverter;
import com.aizuda.snailjob.server.web.service.handler.SyncConfigHandler;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.ConfigAccess;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2022-03-03 11:17
 */
@Service
@RequiredArgsConstructor
public class NotifyConfigServiceImpl implements NotifyConfigService {

    private final AccessTemplate accessTemplate;

    @Override
    public PageResult<List<NotifyConfigResponseVO>> getNotifyConfigList(NotifyConfigQueryVO queryVO) {
        PageDTO<NotifyConfig> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        List<String> groupNames = UserSessionUtils.getGroupNames(queryVO.getGroupName());

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        List<NotifyConfig> notifyConfigs = accessTemplate.getNotifyConfigAccess().listPage(pageDTO,
                        new LambdaQueryWrapper<NotifyConfig>()
                                .eq(NotifyConfig::getNamespaceId, userSessionVO.getNamespaceId())
                                .in(CollUtil.isNotEmpty(groupNames), NotifyConfig::getGroupName, groupNames)
                                .eq(StrUtil.isNotBlank(queryVO.getGroupName()), NotifyConfig::getGroupName, queryVO.getGroupName())
                                .eq(Objects.nonNull(queryVO.getNotifyStatus()), NotifyConfig::getNotifyStatus, queryVO.getNotifyStatus())
                                .eq(Objects.nonNull(queryVO.getSystemTaskType()), NotifyConfig::getSystemTaskType, queryVO.getSystemTaskType())
                                .likeRight(StrUtil.isNotBlank(queryVO.getNotifyName()), NotifyConfig::getNotifyName, queryVO.getNotifyName())
                                .orderByDesc(NotifyConfig::getId))
                .getRecords();

        if (CollUtil.isEmpty(notifyConfigs)) {
            return new PageResult<>(pageDTO, Lists.newArrayList());
        }

        List<NotifyConfigResponseVO> notifyConfigResponseVOS = NotifyConfigResponseVOConverter.INSTANCE.convertList(
                notifyConfigs);

        return new PageResult<>(pageDTO, notifyConfigResponseVOS);
    }

    @Override
    public List<NotifyConfig> getNotifyConfigBySystemTaskTypeList(Integer systemTaskType) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        List<NotifyConfig> notifyConfigList = accessTemplate.getNotifyConfigAccess().list(new LambdaQueryWrapper<NotifyConfig>()
                .select(NotifyConfig::getId, NotifyConfig::getNotifyName)
                .eq(NotifyConfig::getNamespaceId, namespaceId)
                .eq(NotifyConfig::getSystemTaskType, systemTaskType)
                .orderByDesc(NotifyConfig::getId)
        );
        return notifyConfigList;
    }

    @Override
    public Boolean saveNotify(NotifyConfigRequestVO requestVO) {
        NotifyConfig notifyConfig = NotifyConfigConverter.INSTANCE.convert(requestVO);
        notifyConfig.setCreateDt(LocalDateTime.now());
        notifyConfig.setRecipientIds(JsonUtil.toJsonString(requestVO.getRecipientIds()));
        notifyConfig.setNamespaceId(UserSessionUtils.currentUserSession().getNamespaceId());
        ConfigAccess<NotifyConfig> notifyConfigAccess = accessTemplate.getNotifyConfigAccess();

        Assert.isTrue(1 == notifyConfigAccess.insert(notifyConfig),
                () -> new SnailJobServerException("failed to insert notify. sceneConfig:[{}]",
                        JsonUtil.toJsonString(notifyConfig)));
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateNotify(NotifyConfigRequestVO requestVO) {
        Assert.notNull(requestVO.getId(), () -> new SnailJobServerException("Parameter exception"));
        NotifyConfig notifyConfig = NotifyConfigConverter.INSTANCE.convert(requestVO);
        notifyConfig.setRecipientIds(JsonUtil.toJsonString(requestVO.getRecipientIds()));

        // 防止被覆盖
        notifyConfig.setNamespaceId(null);
        Assert.isTrue(1 == accessTemplate.getNotifyConfigAccess().updateById(notifyConfig),
                () -> new SnailJobServerException("failed to update notify. sceneConfig:[{}]",
                        JsonUtil.toJsonString(notifyConfig)));
        return Boolean.TRUE;
    }

    @Override
    public NotifyConfigResponseVO getNotifyConfigDetail(Long id) {
        NotifyConfig notifyConfig = accessTemplate.getNotifyConfigAccess().one(new LambdaQueryWrapper<NotifyConfig>()
                .eq(NotifyConfig::getId, id));
        return NotifyConfigResponseVOConverter.INSTANCE.convert(notifyConfig);
    }

    @Override
    public Boolean updateStatus(final Long id, final Integer status) {

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        NotifyConfig notifyConfig = accessTemplate.getNotifyConfigAccess().one(
                new LambdaQueryWrapper<NotifyConfig>()
                        .eq(NotifyConfig::getId, id)
                        .eq(NotifyConfig::getNamespaceId, namespaceId)
        );
        Assert.notNull(notifyConfig, () -> new SnailJobServerException("Notification configuration does not exist"));

        // 同步配置到客户端
        SyncConfigHandler.addSyncTask(notifyConfig.getGroupName(), namespaceId);

        NotifyConfig config = new NotifyConfig();
        config.setNotifyStatus(status);
        config.setUpdateDt(LocalDateTime.now());
        int update = accessTemplate.getNotifyConfigAccess()
                .update(config, new LambdaUpdateWrapper<NotifyConfig>()
                        .eq(NotifyConfig::getNamespaceId, namespaceId)
                        .eq(NotifyConfig::getId, id)
                );

        return 1 == update;
    }

    @Override
    public Boolean batchDeleteNotify(final Set<Long> ids) {
        return ids.size() == accessTemplate.getNotifyConfigAccess()
                .delete(new LambdaQueryWrapper<NotifyConfig>().in(NotifyConfig::getId, ids));
    }
}
