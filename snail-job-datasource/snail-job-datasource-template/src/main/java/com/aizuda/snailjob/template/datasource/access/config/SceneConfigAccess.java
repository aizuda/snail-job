package com.aizuda.snailjob.template.datasource.access.config;

import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.enums.OperationTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
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
public class SceneConfigAccess extends AbstractConfigAccess<RetrySceneConfig> {

    @Override
    public boolean supports(String operationType) {
        DbTypeEnum dbType = getDbType();
        return OperationTypeEnum.SCENE.name().equals(operationType)
                && dbType.isAllowDb();
    }

    @Override
    public List<RetrySceneConfig> list(LambdaQueryWrapper<RetrySceneConfig> query) {
        return sceneConfigMapper.selectList(query);
    }

    @Override
    public int update(RetrySceneConfig retrySceneConfig, LambdaUpdateWrapper<RetrySceneConfig> query) {
        return sceneConfigMapper.update(retrySceneConfig, query);
    }

    @Override
    public int updateById(RetrySceneConfig retrySceneConfig) {
        return sceneConfigMapper.updateById(retrySceneConfig);
    }

    @Override
    public int delete(LambdaQueryWrapper<RetrySceneConfig> query) {
        return sceneConfigMapper.delete(query);
    }

    @Override
    public int insert(RetrySceneConfig retrySceneConfig) {
        return sceneConfigMapper.insert(retrySceneConfig);
    }

    @Override
    public RetrySceneConfig one(LambdaQueryWrapper<RetrySceneConfig> query) {
        return sceneConfigMapper.selectOne(query);
    }

    @Override
    public PageDTO<RetrySceneConfig> listPage(PageDTO<RetrySceneConfig> iPage, LambdaQueryWrapper<RetrySceneConfig> query) {
        return sceneConfigMapper.selectPage(iPage, query);
    }

    @Override
    public long count(LambdaQueryWrapper<RetrySceneConfig> query) {
        return sceneConfigMapper.selectCount(query);
    }
}
