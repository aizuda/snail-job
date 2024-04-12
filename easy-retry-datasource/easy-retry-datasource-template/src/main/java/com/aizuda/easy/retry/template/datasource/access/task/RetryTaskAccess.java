package com.aizuda.easy.retry.template.datasource.access.task;

import com.aizuda.easy.retry.template.datasource.enums.DbTypeEnum;
import com.aizuda.easy.retry.template.datasource.enums.OperationTypeEnum;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
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
public class RetryTaskAccess extends AbstractTaskAccess<RetryTask> {

    @Autowired
    private RetryTaskMapper retryTaskMapper;

    @Override
    public boolean supports(String operationType) {
        DbTypeEnum dbType = getDbType();
        return OperationTypeEnum.RETRY_TASK.name().equals(operationType)
                && ALLOW_DB.contains(dbType.getDb());
    }

    @Override
    protected int doUpdate(RetryTask retryTask, LambdaUpdateWrapper<RetryTask> query) {
        return retryTaskMapper.update(retryTask, query);
    }

    @Override
    protected int doBatchInsert(List<RetryTask> list) {
        return retryTaskMapper.batchInsert(list);
    }

    @Override
    protected RetryTask doOne(LambdaQueryWrapper<RetryTask> query) {
        return retryTaskMapper.selectOne(query);
    }

    @Override
    protected PageDTO<RetryTask> doListPage(final PageDTO<RetryTask> iPage, final LambdaQueryWrapper<RetryTask> query) {
        return retryTaskMapper.selectPage(iPage, query);
    }

    @Override
    protected long doCount(final LambdaQueryWrapper<RetryTask> query) {
        return retryTaskMapper.selectCount(query);
    }

    @Override
    protected int doInsert(RetryTask retryTask) {
        return retryTaskMapper.insert(retryTask);
    }

    @Override
    protected int doDelete(LambdaQueryWrapper<RetryTask> query) {
        return retryTaskMapper.delete(query);
    }

    @Override
    protected int doUpdateById(RetryTask retryTask) {
        return retryTaskMapper.updateById(retryTask);
    }

    @Override
    protected List<RetryTask> doList(LambdaQueryWrapper<RetryTask> query) {
        return retryTaskMapper.selectList(query);
    }
}
