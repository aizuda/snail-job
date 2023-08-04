package com.aizuda.easy.retry.template.datasource.access.task;

import com.aizuda.easy.retry.template.datasource.enums.DbTypeEnum;
import com.aizuda.easy.retry.template.datasource.enums.OperationTypeEnum;
import com.aizuda.easy.retry.template.datasource.exception.EasyRetryDatasourceException;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryDeadLetterMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryDeadLetter;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-08-06 10:18:02
 * @since 2.2.0
 */
@Component
public class RetryDeadLetterTaskAccess  extends AbstractTaskAccess<RetryDeadLetter> {

    @Autowired
    private RetryDeadLetterMapper retryDeadLetterMapper;

    @Override
    public boolean supports(String operationType) {
        DbTypeEnum dbType = getDbType();
        return OperationTypeEnum.RETRY_TASK.name().equals(operationType)
                && ALLOW_DB.contains(dbType.getDb());
    }

    @Override
    public List<RetryDeadLetter> listAvailableTasks(String groupName, LocalDateTime lastAt, Long lastId, Integer pageSize, Integer taskType) {
        throw new EasyRetryDatasourceException("不支持查询");
    }

    @Override
    protected int doUpdate(RetryDeadLetter retryDeadLetter, LambdaUpdateWrapper<RetryDeadLetter> query) {
        return retryDeadLetterMapper.update(retryDeadLetter, query);
    }

    @Override
    protected int doInsert(RetryDeadLetter retryDeadLetter) {
        return retryDeadLetterMapper.insert(retryDeadLetter);
    }

    @Override
    protected int doDelete(LambdaQueryWrapper<RetryDeadLetter> query) {
        return retryDeadLetterMapper.delete(query);
    }

    @Override
    protected int doUpdateById(RetryDeadLetter retryDeadLetter) {
        return retryDeadLetterMapper.updateById(retryDeadLetter);
    }

    @Override
    protected List<RetryDeadLetter> doList(LambdaQueryWrapper<RetryDeadLetter> query) {
        return retryDeadLetterMapper.selectList(query);
    }
}