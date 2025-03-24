package com.aizuda.snailjob.server.common.convert;

import com.aizuda.snailjob.server.common.dto.JobLogDTO;
import com.aizuda.snailjob.server.model.dto.LogTaskDTO;
import com.aizuda.snailjob.template.datasource.persistence.po.JobLogMessage;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.server.common.support
 * @Project：snail-job
 * @Date：2025/3/10 21:16
 * @Filename：JobLogMessageConverter
 * @since 1.5.0
 */
@Mapper
public interface JobLogMessageConverter {

    JobLogMessageConverter INSTANCE = Mappers.getMapper(JobLogMessageConverter.class);

    JobLogMessage toJobLogMessage(JobLogDTO jobLogDTO);

    JobLogMessage toJobLogMessage(LogTaskDTO logTaskDTO);
}
