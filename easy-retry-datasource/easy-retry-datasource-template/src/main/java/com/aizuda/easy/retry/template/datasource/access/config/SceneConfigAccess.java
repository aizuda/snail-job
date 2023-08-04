package com.aizuda.easy.retry.template.datasource.access.config;

import com.aizuda.easy.retry.template.datasource.enums.DbTypeEnum;
import com.aizuda.easy.retry.template.datasource.enums.OperationTypeEnum;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-08-05 23:17:21
 * @since 2.2.0
 */
@Component
public class SceneConfigAccess extends AbstractConfigAccess<SceneConfig> {

    @Override
    public boolean supports(String operationType) {
        DbTypeEnum dbType = getDbType();
        return OperationTypeEnum.SCENE.name().equals(operationType)
                && ALLOW_DB.contains(dbType.getDb());
    }

    @Override
    public List<SceneConfig> list(LambdaQueryWrapper<SceneConfig> query) {
        return sceneConfigMapper.selectList(query);
    }

    @Override
    public int update(SceneConfig sceneConfig, LambdaUpdateWrapper<SceneConfig> query) {
        return sceneConfigMapper.update(sceneConfig, query);
    }

    @Override
    public int updateById(SceneConfig sceneConfig) {
        return sceneConfigMapper.updateById(sceneConfig);
    }

    @Override
    public int delete(LambdaQueryWrapper<SceneConfig> query) {
        return sceneConfigMapper.delete(query);
    }

    @Override
    public int insert(SceneConfig sceneConfig) {
        return sceneConfigMapper.insert(sceneConfig);
    }
}
