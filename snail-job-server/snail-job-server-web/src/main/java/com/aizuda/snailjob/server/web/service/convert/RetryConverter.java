package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.server.retry.task.dto.RetryTaskPrepareDTO;
import com.aizuda.snailjob.server.retry.task.dto.TaskStopJobDTO;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-02-22
 */
@Mapper
public interface RetryConverter {
    RetryConverter INSTANCE = Mappers.getMapper(RetryConverter.class);

    RetryTaskPrepareDTO toRetryTaskPrepareDTO(Retry retry);

    TaskStopJobDTO toTaskStopJobDTO(Retry retry);
}
