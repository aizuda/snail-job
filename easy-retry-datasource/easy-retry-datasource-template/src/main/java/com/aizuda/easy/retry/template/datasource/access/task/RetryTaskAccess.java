package com.aizuda.easy.retry.template.datasource.access.task;

import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.template.datasource.enums.DbTypeEnum;
import com.aizuda.easy.retry.template.datasource.enums.OperationTypeEnum;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author www.byteblogs.com
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
    public List<RetryTask> listAvailableTasks(String groupName,
                                              LocalDateTime lastAt,
                                              Long lastId,
                                              Integer pageSize,
                                              Integer taskType) {
        setPartition(groupName);
        return retryTaskMapper.selectPage(new PageDTO<>(0, pageSize),
                        new LambdaQueryWrapper<RetryTask>()
                                .eq(RetryTask::getRetryStatus, RetryStatusEnum.RUNNING.getStatus())
                                .eq(RetryTask::getGroupName, groupName)
                                .eq(RetryTask::getTaskType, taskType)
                                .gt(RetryTask::getId, lastId)
                                .gt(RetryTask::getCreateDt, lastAt)
                                .orderByAsc(RetryTask::getId)
                                .orderByAsc(RetryTask::getCreateDt))
                .getRecords();
    }

    @Override
    protected int doUpdate(RetryTask retryTask, LambdaUpdateWrapper<RetryTask> query) {
        return retryTaskMapper.update(retryTask, query);
    }

    @Override
    protected IPage<RetryTask> doListPage(final IPage<RetryTask> iPage, final LambdaQueryWrapper<RetryTask> query) {
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
