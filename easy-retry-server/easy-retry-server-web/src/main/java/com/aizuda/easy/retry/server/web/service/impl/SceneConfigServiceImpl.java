package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.SceneConfigMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import com.aizuda.easy.retry.server.web.service.convert.SceneConfigResponseVOConverter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.aizuda.easy.retry.server.web.service.SceneConfigService;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.SceneConfigQueryVO;
import com.aizuda.easy.retry.server.web.model.response.SceneConfigResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 10:55
 */
@Service
public class SceneConfigServiceImpl implements SceneConfigService {

    @Autowired
    private SceneConfigMapper sceneConfigMapper;

    @Override
    public PageResult<List<SceneConfigResponseVO>> getSceneConfigPageList(SceneConfigQueryVO queryVO) {
        PageDTO<SceneConfig> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        LambdaQueryWrapper<SceneConfig> sceneConfigLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(queryVO.getSceneName())) {
            sceneConfigLambdaQueryWrapper.eq(SceneConfig::getSceneName, queryVO.getSceneName());
        }

        pageDTO = sceneConfigMapper.selectPage(pageDTO, sceneConfigLambdaQueryWrapper
                .eq(SceneConfig::getGroupName, queryVO.getGroupName()).orderByDesc(SceneConfig::getCreateDt));

        return new PageResult<>(pageDTO, SceneConfigResponseVOConverter.INSTANCE.batchConvert(pageDTO.getRecords()));

    }

    @Override
    public List<SceneConfigResponseVO> getSceneConfigList(String groupName) {

        List<SceneConfig> sceneConfigs = sceneConfigMapper.selectList(new LambdaQueryWrapper<SceneConfig>()
                .select(SceneConfig::getSceneName, SceneConfig::getDescription)
                .eq(SceneConfig::getGroupName, groupName).orderByDesc(SceneConfig::getCreateDt));

        return SceneConfigResponseVOConverter.INSTANCE.batchConvert(sceneConfigs);
    }
}
