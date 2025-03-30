package com.aizuda.snailjob.template.datasource.access.log;

import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.JobLogMessageDO;
import com.aizuda.snailjob.template.datasource.persistence.po.JobLogMessage;
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

}
