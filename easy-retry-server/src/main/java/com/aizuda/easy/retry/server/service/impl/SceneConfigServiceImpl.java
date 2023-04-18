package com.aizuda.easy.retry.server.service.impl;

import com.aizuda.easy.retry.server.persistence.mybatis.mapper.SceneConfigMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.SceneConfig;
import com.aizuda.easy.retry.server.service.convert.SceneConfigResponseVOConverter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.aizuda.easy.retry.server.service.SceneConfigService;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.SceneConfigQueryVO;
import com.aizuda.easy.retry.server.web.model.response.SceneConfigResponseVO;
import org.apache.commons.lang.StringUtils;
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

    private SceneConfigResponseVOConverter sceneConfigResponseVOConverter = new SceneConfigResponseVOConverter();

    @Override
    public PageResult<List<SceneConfigResponseVO>> getSceneConfigPageList(SceneConfigQueryVO queryVO) {
        PageDTO<SceneConfig> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        LambdaQueryWrapper<SceneConfig> sceneConfigLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(queryVO.getSceneName())) {
            sceneConfigLambdaQueryWrapper.eq(SceneConfig::getSceneName, queryVO.getSceneName());
        }

        pageDTO = sceneConfigMapper.selectPage(pageDTO, sceneConfigLambdaQueryWrapper
                .eq(SceneConfig::getGroupName, queryVO.getGroupName()).orderByDesc(SceneConfig::getCreateDt));

        return new PageResult<>(pageDTO, sceneConfigResponseVOConverter.batchConvert(pageDTO.getRecords()));

    }

    @Override
    public List<SceneConfigResponseVO> getSceneConfigList(String groupName) {

        List<SceneConfig> sceneConfigs = sceneConfigMapper.selectList(new LambdaQueryWrapper<SceneConfig>()
                .select(SceneConfig::getSceneName, SceneConfig::getDescription)
                .eq(SceneConfig::getGroupName, groupName).orderByDesc(SceneConfig::getCreateDt));

        return sceneConfigResponseVOConverter.batchConvert(sceneConfigs);
    }
}
