package com.aizuda.snailjob.template.datasource.access.log;

import com.aizuda.snailjob.template.datasource.access.LogAccess;
import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.enums.OperationTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.common.*;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.JobLogMessageDO;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.LogPageQueryDO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobLogMessage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

import static com.aizuda.snailjob.template.datasource.utils.DbUtils.getDbType;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-03-30
 */
@Component
@Slf4j
//@ConditionalOnMissingBean(LogAccess.class)
@RequiredArgsConstructor
public class JobLogMessageAccess extends AbstractLogAccess<JobLogMessageDO> {
    private final JobLogMessageMapper jobLogMessageMapper;
    private final JobTaskBatchMapper jobTaskBatchMapper;

    @Override
    public boolean supports(String operationType) {
        return DbTypeEnum.all().contains(getDbType()) && OperationTypeEnum.JOB_LOG.name().equals(operationType);
    }

    @Override
    public int insert(JobLogMessageDO jobLogMessageDO) {
        JobLogMessage jobLogMessage = LogConverter.INSTANCE.toJobLogMessage(jobLogMessageDO);
        return jobLogMessageMapper.insert(jobLogMessage);
    }

    @Override
    public int insertBatch(List<JobLogMessageDO> list) {
        List<JobLogMessage> jobLogMessages = LogConverter.INSTANCE.toJobLogMessages(list);
        return jobLogMessageMapper.insertBatch(jobLogMessages);
    }

    @Override
    public PageResponseDO<JobLogMessageDO> listPage(PageQueryDO queryDO) {
        LogPageQueryDO logPageQueryDO = (LogPageQueryDO) queryDO;

        PageDTO<JobLogMessage> selectPage = jobLogMessageMapper.selectPage(
                new PageDTO<>(queryDO.getPage(), logPageQueryDO.getSize()),
                new LambdaQueryWrapper<JobLogMessage>()
                        .ge(JobLogMessage::getId, logPageQueryDO.getStartId())
                        .eq(JobLogMessage::getTaskBatchId, logPageQueryDO.getTaskBatchId())
                        .eq(JobLogMessage::getTaskId, logPageQueryDO.getTaskId())
                        .orderByAsc(JobLogMessage::getId).orderByAsc(JobLogMessage::getRealTime));
        List<JobLogMessage> records = selectPage.getRecords();

        PageResponseDO<JobLogMessageDO> responseDO = new PageResponseDO<>();
        responseDO.setPage(selectPage.getCurrent());
        responseDO.setSize(selectPage.getSize());
        responseDO.setTotal(selectPage.getTotal());
        responseDO.setRows(LogConverter.INSTANCE.toJobLogMessageDOList(records));
        return responseDO;
    }

    @Override
    public List<JobLogMessageDO> list(ListQueryDO queryDO) {
        return List.of();
    }

    @Override
    public JobLogMessageDO one(OneQueryDO query) {
        return null;
    }

    @Override
    public int update(JobLogMessageDO jobLogMessageDO, UpdateQueryDO query) {
        return 0;
    }

    @Override
    public int updateById(JobLogMessageDO jobLogMessageDO) {
        JobLogMessage jobLogMessage = LogConverter.INSTANCE.toJobLogMessage(jobLogMessageDO);
        return jobLogMessageMapper.updateById(jobLogMessage);
    }

    @Override
    public int deleteById(Serializable id) {
        return jobLogMessageMapper.deleteById(id);
    }

    @Override
    public int delete(DeleteQueryDO query) {
        return 0;
    }

    @Override
    public long count(LambdaQueryWrapper<JobLogMessageDO> query) {
        return 0;
    }

}
