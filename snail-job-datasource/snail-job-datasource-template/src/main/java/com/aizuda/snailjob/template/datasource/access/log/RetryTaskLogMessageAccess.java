package com.aizuda.snailjob.template.datasource.access.log;

import com.aizuda.snailjob.template.datasource.access.RetryLogAccess;
import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.enums.OperationTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.common.*;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.RetryTaskLogMessageDO;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.RetryTaskLogMessageQueryDO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLogMessage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class RetryTaskLogMessageAccess implements RetryLogAccess<RetryTaskLogMessageDO> {

    private final RetryTaskLogMessageMapper retryTaskLogMessageMapper;

    @Override
    public boolean supports(String operationType) {
        return DbTypeEnum.all().contains(getDbType()) && OperationTypeEnum.RETRY_LOG.name().equals(operationType);

    }
    @Override
    public int insert(RetryTaskLogMessageDO retryTaskLogMessageDO) {
        RetryTaskLogMessage retryTaskLogMessage = LogConverter.INSTANCE.toRetryTaskLogMessage(retryTaskLogMessageDO);
        return retryTaskLogMessageMapper.insert(retryTaskLogMessage);
    }

    @Override
    public int insertBatch(List<RetryTaskLogMessageDO> list) {
        List<RetryTaskLogMessage> retryTaskMessages = LogConverter.INSTANCE.toRetryTaskMessages(list);
        return retryTaskLogMessageMapper.insertBatch(retryTaskMessages);
    }

    @Override
    public PageResponseDO listPage(PageQueryDO queryDO) {
        RetryTaskLogMessageQueryDO logPageQueryDO = (RetryTaskLogMessageQueryDO) queryDO;
        PageDTO<RetryTaskLogMessage> selectPage = retryTaskLogMessageMapper.selectPage(
                new PageDTO<>(queryDO.getPage(), logPageQueryDO.getSize()),
                new LambdaQueryWrapper<RetryTaskLogMessage>()
                        .ge(RetryTaskLogMessage::getId, logPageQueryDO.getStartId())
                        .eq(RetryTaskLogMessage::getRetryTaskId, logPageQueryDO.getRetryTaskId())
                        .orderByAsc(RetryTaskLogMessage::getId).orderByAsc(RetryTaskLogMessage::getRealTime));
        List<RetryTaskLogMessage> records = selectPage.getRecords();

        PageResponseDO<RetryTaskLogMessageDO> responseDO = new PageResponseDO<>();
        responseDO.setPage(selectPage.getCurrent());
        responseDO.setSize(selectPage.getSize());
        responseDO.setTotal(selectPage.getTotal());
        responseDO.setRows(LogConverter.INSTANCE.toRetryTaskLogMessageDOList(records));
        return responseDO;
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
