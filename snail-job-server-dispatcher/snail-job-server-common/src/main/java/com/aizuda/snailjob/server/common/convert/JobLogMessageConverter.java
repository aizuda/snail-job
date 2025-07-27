package com.aizuda.snailjob.server.common.convert;

import com.aizuda.snailjob.model.request.JobLogTaskRequest;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.JobLogMessageDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.server.common.support
 * @Project：snail-job
 * @Date：2025/3/10 21:16
 * @Filename：JobLogMessageConverter
 * @since 1.5.0
 */
@Mapper
@Deprecated
public interface JobLogMessageConverter {

    JobLogMessageConverter INSTANCE = Mappers.getMapper(JobLogMessageConverter.class);


    List<JobLogMessageDO> toJobLogMessages(List<JobLogTaskRequest> jobLogDTOs);

}
