package com.aizuda.snailjob.template.datasource.access.config;

import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.enums.OperationTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-08-05 23:05:25
 * @since 2.2.0
 */
@Component
public class NotifyConfigAccess extends AbstractConfigAccess<NotifyConfig>  {

    @Override
    public boolean supports(String operationType) {
        DbTypeEnum dbType = getDbType();
        return OperationTypeEnum.NOTIFY.name().equals(operationType)
                && ALLOW_DB.contains(dbType.getDb());
    }

    @Override
    public List<NotifyConfig> list(LambdaQueryWrapper<NotifyConfig> query) {
        return notifyConfigMapper.selectList(query);
    }

    @Override
    public int update(NotifyConfig notifyConfig, LambdaUpdateWrapper<NotifyConfig> query) {
        return notifyConfigMapper.update(notifyConfig, query);
    }

    @Override
    public int updateById(NotifyConfig notifyConfig) {
        return notifyConfigMapper.updateById(notifyConfig);
    }

    @Override
    public int delete(LambdaQueryWrapper<NotifyConfig> query) {
        return notifyConfigMapper.delete(query);
    }

    @Override
    public int insert(NotifyConfig notifyConfig) {
        return notifyConfigMapper.insert(notifyConfig);
    }

    @Override
    public NotifyConfig one(LambdaQueryWrapper<NotifyConfig> query) {
        return notifyConfigMapper.selectOne(query);
    }

    @Override
    public PageDTO<NotifyConfig> listPage(PageDTO<NotifyConfig> iPage, LambdaQueryWrapper<NotifyConfig> query) {
        return notifyConfigMapper.selectPage(iPage, query);
    }

    @Override
    public long count(LambdaQueryWrapper<NotifyConfig> query) {
        return notifyConfigMapper.selectCount(query);
    }

}
