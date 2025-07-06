package com.aizuda.snailjob.server.openapi.job.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-05
 */
@Mapper
public interface JobApiConverter {
    JobApiConverter INSTANCE = Mappers.getMapper(JobApiConverter.class);
}

