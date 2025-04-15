package com.aizuda.snailjob.template.datasource.access.log;

import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.JobLogMessageDO;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.RetryTaskLogMessageDO;
import com.aizuda.snailjob.template.datasource.persistence.po.JobLogMessage;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLogMessage;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-03-30
 */
@Mapper
public interface LogConverter {

    LogConverter INSTANCE = Mappers.getMapper(LogConverter.class);

    JobLogMessage toJobLogMessage(JobLogMessageDO logMessage);

    List<JobLogMessage> toJobLogMessages(List<JobLogMessageDO> logMessages);

    JobLogMessageDO toJobLogMessageDO(JobLogMessage logMessage);

    List<JobLogMessageDO> toJobLogMessageDOList(List<JobLogMessage> logMessages);

    RetryTaskLogMessage toRetryTaskLogMessage(RetryTaskLogMessageDO logMessage);

    List<RetryTaskLogMessage> toRetryTaskMessages(List<RetryTaskLogMessageDO> logMessages);

    RetryTaskLogMessageDO toRetryTaskLogMessageDO(RetryTaskLogMessage logMessage);

    List<RetryTaskLogMessageDO> toRetryTaskLogMessageDOList(List<RetryTaskLogMessage> logMessages);

}
