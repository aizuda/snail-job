package com.aizuda.snailjob.template.datasource.access.task;

import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.enums.OperationTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 重试任务操作类
 *
 * @author opensnail
 * @date 2023-08-05 23:28:20
 * @since 2.2.0
 */
@Component
public class RetryAccess extends AbstractTaskAccess<Retry> {

    @Autowired
    private RetryMapper retryMapper;

    @Override
    public boolean supports(String operationType) {
        DbTypeEnum dbType = getDbType();
        return OperationTypeEnum.RETRY.name().equals(operationType)
                && ALLOW_DB.contains(dbType.getDb());
    }

    @Override
    protected int doUpdate(Retry retry, LambdaUpdateWrapper<Retry> query) {
        return retryMapper.update(retry, query);
    }

    @Override
    protected int doInsertBatch(List<Retry> list) {
        return retryMapper.insertBatch(list);
    }

    @Override
    protected Retry doOne(LambdaQueryWrapper<Retry> query) {
        return retryMapper.selectOne(query);
    }

    @Override
    protected PageDTO<Retry> doListPage(final PageDTO<Retry> iPage, final LambdaQueryWrapper<Retry> query) {
        return retryMapper.selectPage(iPage, query);
    }

    @Override
    protected long doCount(final LambdaQueryWrapper<Retry> query) {
        return retryMapper.selectCount(query);
    }

    @Override
    protected int doInsert(Retry retry) {
        return retryMapper.insert(retry);
    }

    @Override
    protected int doDelete(LambdaQueryWrapper<Retry> query) {
        return retryMapper.delete(query);
    }

    @Override
    protected int doUpdateById(Retry retry) {
        return retryMapper.updateById(retry);
    }

    @Override
    protected List<Retry> doList(LambdaQueryWrapper<Retry> query) {
        return retryMapper.selectList(query);
    }
}
