package com.aizuda.snailjob.template.datasource.access.log;

import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.enums.OperationTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.common.*;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.RetryTaskLogMessageDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

import static com.aizuda.snailjob.template.datasource.utils.DbUtils.getDbType;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-03-29
 */
@Component
public class RetryTaskLogMessageAccess extends AbstractLogAccess<RetryTaskLogMessageDO> {


    @Override
    public boolean supports(String operationType) {
        return DbTypeEnum.all().contains(getDbType()) && OperationTypeEnum.RETRY_LOG.name().equals(operationType);

    }
    @Override
    public int insert(RetryTaskLogMessageDO retryTaskLogMessageDO) {
        return 0;
    }

    @Override
    public int insertBatch(List<RetryTaskLogMessageDO> list) {
        return 0;
    }

    @Override
    public PageResponseDO listPage(PageQueryDO queryDO) {
        return null;
    }

    @Override
    public List<RetryTaskLogMessageDO> list(ListQueryDO queryDO) {
        return List.of();
    }

    @Override
    public RetryTaskLogMessageDO one(OneQueryDO query) {
        return null;
    }

    @Override
    public int update(RetryTaskLogMessageDO retryTaskLogMessageDO, UpdateQueryDO query) {
        return 0;
    }

    @Override
    public int updateById(RetryTaskLogMessageDO retryTaskLogMessageDO) {
        return 0;
    }

    @Override
    public int deleteById(Serializable id) {
        return 0;
    }

    @Override
    public int delete(DeleteQueryDO query) {
        return 0;
    }

    @Override
    public long count(LambdaQueryWrapper<RetryTaskLogMessageDO> query) {
        return 0;
    }


}
