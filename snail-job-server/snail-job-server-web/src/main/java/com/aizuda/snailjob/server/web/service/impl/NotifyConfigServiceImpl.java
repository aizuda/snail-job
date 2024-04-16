package com.aizuda.snailjob.server.web.service.impl;

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
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.ConfigAccess;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
        PageDTO<NotifyConfig> pageDTO = new PageDTO<>();

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        LambdaQueryWrapper<NotifyConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NotifyConfig::getNamespaceId, userSessionVO.getNamespaceId());

        if (userSessionVO.isUser()) {
            queryWrapper.in(NotifyConfig::getGroupName, userSessionVO.getGroupNames());
        }

        if (StrUtil.isNotBlank(queryVO.getGroupName())) {
            queryWrapper.eq(NotifyConfig::getGroupName, queryVO.getGroupName());
        }
        if (StrUtil.isNotBlank(queryVO.getSceneName())) {
            queryWrapper.eq(NotifyConfig::getBusinessId, queryVO.getSceneName());
        }

        queryWrapper.orderByDesc(NotifyConfig::getId);
        List<NotifyConfig> notifyConfigs = accessTemplate.getNotifyConfigAccess().listPage(pageDTO, queryWrapper).getRecords();
        return new PageResult<>(pageDTO, NotifyConfigResponseVOConverter.INSTANCE.batchConvert(notifyConfigs));
    }

    @Override
    public Boolean saveNotify(NotifyConfigRequestVO requestVO) {
        NotifyConfig notifyConfig = NotifyConfigConverter.INSTANCE.toNotifyConfig(requestVO);
        notifyConfig.setCreateDt(LocalDateTime.now());
        notifyConfig.setNamespaceId(UserSessionUtils.currentUserSession().getNamespaceId());
        ConfigAccess<NotifyConfig> notifyConfigAccess = accessTemplate.getNotifyConfigAccess();

        Assert.isTrue(1 == notifyConfigAccess.insert(notifyConfig),
                () -> new SnailJobServerException("failed to insert notify. sceneConfig:[{}]", JsonUtil.toJsonString(notifyConfig)));
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateNotify(NotifyConfigRequestVO requestVO) {
        Assert.notNull(requestVO.getId(), () -> new SnailJobServerException("参数异常"));
        NotifyConfig notifyConfig = NotifyConfigConverter.INSTANCE.toNotifyConfig(requestVO);
        // 防止被覆盖
        notifyConfig.setNamespaceId(null);
        Assert.isTrue(1 == accessTemplate.getNotifyConfigAccess().updateById(notifyConfig),
                () -> new SnailJobServerException("failed to update notify. sceneConfig:[{}]", JsonUtil.toJsonString(notifyConfig)));
        return Boolean.TRUE;
    }

    @Override
    public NotifyConfigResponseVO getNotifyConfigDetail(Long id) {
        NotifyConfig notifyConfig = accessTemplate.getNotifyConfigAccess().one(new LambdaQueryWrapper<NotifyConfig>()
                .eq(NotifyConfig::getId, id));
        return NotifyConfigResponseVOConverter.INSTANCE.convert(notifyConfig);
    }
}
