package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.SceneConfigQueryVO;
import com.aizuda.easy.retry.server.web.model.request.SceneConfigRequestVO;
import com.aizuda.easy.retry.server.web.model.response.SceneConfigResponseVO;
import com.aizuda.easy.retry.server.web.service.SceneConfigService;
import com.aizuda.easy.retry.server.web.service.convert.SceneConfigConverter;
import com.aizuda.easy.retry.server.web.service.convert.SceneConfigResponseVOConverter;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.access.ConfigAccess;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 10:55
 */
@Service
public class SceneConfigServiceImpl implements SceneConfigService {

    @Autowired
    private AccessTemplate accessTemplate;

    @Override
    public PageResult<List<SceneConfigResponseVO>> getSceneConfigPageList(SceneConfigQueryVO queryVO) {
        PageDTO<SceneConfig> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        LambdaQueryWrapper<SceneConfig> sceneConfigLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(queryVO.getGroupName())) {
            sceneConfigLambdaQueryWrapper.eq(SceneConfig::getGroupName, queryVO.getGroupName().trim());
        }

        if (StrUtil.isNotBlank(queryVO.getSceneName())) {
            sceneConfigLambdaQueryWrapper.eq(SceneConfig::getSceneName, queryVO.getSceneName().trim());
        }

        pageDTO = accessTemplate.getSceneConfigAccess().listPage(pageDTO, sceneConfigLambdaQueryWrapper.orderByDesc(SceneConfig::getCreateDt));

        return new PageResult<>(pageDTO, SceneConfigResponseVOConverter.INSTANCE.batchConvert(pageDTO.getRecords()));

    }

    @Override
    public List<SceneConfigResponseVO> getSceneConfigList(String groupName) {

        List<SceneConfig> sceneConfigs = accessTemplate.getSceneConfigAccess().list(new LambdaQueryWrapper<SceneConfig>()
                .select(SceneConfig::getSceneName, SceneConfig::getDescription)
                .eq(SceneConfig::getGroupName, groupName).orderByDesc(SceneConfig::getCreateDt));

        return SceneConfigResponseVOConverter.INSTANCE.batchConvert(sceneConfigs);
    }

    @Override
    public Boolean saveSceneConfig(SceneConfigRequestVO requestVO) {
        SceneConfig sceneConfig = SceneConfigConverter.INSTANCE.toSceneConfigRequestVO(requestVO);
        sceneConfig.setCreateDt(LocalDateTime.now());
        ConfigAccess<SceneConfig> sceneConfigAccess = accessTemplate.getSceneConfigAccess();
        Assert.isTrue(1 == sceneConfigAccess.insert(sceneConfig),
                () -> new EasyRetryServerException("failed to insert scene. sceneConfig:[{}]", JsonUtil.toJsonString(sceneConfig)));
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateSceneConfig(SceneConfigRequestVO requestVO) {
        SceneConfig sceneConfig = SceneConfigConverter.INSTANCE.toSceneConfigRequestVO(requestVO);

        Assert.isTrue(1 == accessTemplate.getSceneConfigAccess().update(sceneConfig,
                        new LambdaUpdateWrapper<SceneConfig>()
                                .eq(SceneConfig::getGroupName, sceneConfig.getGroupName())
                                .eq(SceneConfig::getSceneName, sceneConfig.getSceneName())),
                () -> new EasyRetryServerException("failed to update scene. sceneConfig:[{}]", JsonUtil.toJsonString(sceneConfig)));
        return Boolean.TRUE;
    }

    @Override
    public SceneConfigResponseVO getSceneConfigDetail(Long id) {
        SceneConfig sceneConfig = accessTemplate.getSceneConfigAccess().one(new LambdaQueryWrapper<SceneConfig>()
                .eq(SceneConfig::getId, id));
        return SceneConfigResponseVOConverter.INSTANCE.convert(sceneConfig);
    }
}
