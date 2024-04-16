package com.aizuda.snailjob.template.datasource.access.config;

import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.enums.OperationTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.po.SceneConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.SceneConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author opensnail
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

    @Override
    public SceneConfig one(LambdaQueryWrapper<SceneConfig> query) {
        return sceneConfigMapper.selectOne(query);
    }

    @Override
    public PageDTO<SceneConfig> listPage(PageDTO<SceneConfig> iPage, LambdaQueryWrapper<SceneConfig> query) {
        return sceneConfigMapper.selectPage(iPage, query);
    }

    @Override
    public long count(LambdaQueryWrapper<SceneConfig> query) {
        return sceneConfigMapper.selectCount(query);
    }
}
