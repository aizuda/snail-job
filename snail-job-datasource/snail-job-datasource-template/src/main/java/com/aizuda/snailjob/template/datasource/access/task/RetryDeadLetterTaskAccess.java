package com.aizuda.snailjob.template.datasource.access.task;

import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.enums.OperationTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryDeadLetterMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryDeadLetter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-08-06 10:18:02
 * @since 2.2.0
 */
@Component
public class RetryDeadLetterTaskAccess extends AbstractTaskAccess<RetryDeadLetter> {

    @Autowired
    private RetryDeadLetterMapper retryDeadLetterMapper;

    @Override
    public boolean supports(String operationType) {
        DbTypeEnum dbType = getDbType();
        return OperationTypeEnum.RETRY_DEAD_LETTER.name().equals(operationType)
                && ALLOW_DB.contains(dbType.getDb());
    }

    @Override
    protected int doUpdate(RetryDeadLetter retryDeadLetter, LambdaUpdateWrapper<RetryDeadLetter> query) {
        return retryDeadLetterMapper.update(retryDeadLetter, query);
    }

    @Override
    protected int doInsertBatch(List<RetryDeadLetter> list) {
        return retryDeadLetterMapper.insertBatch(list);
    }

    @Override
    protected RetryDeadLetter doOne(LambdaQueryWrapper<RetryDeadLetter> query) {
        return retryDeadLetterMapper.selectOne(query);
    }

    @Override
    protected PageDTO<RetryDeadLetter> doListPage(final PageDTO<RetryDeadLetter> PageDTO,
                                                  final LambdaQueryWrapper<RetryDeadLetter> query) {
        return retryDeadLetterMapper.selectPage(PageDTO, query);
    }

    @Override
    protected long doCount(final LambdaQueryWrapper<RetryDeadLetter> query) {
        return retryDeadLetterMapper.selectCount(query);
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
