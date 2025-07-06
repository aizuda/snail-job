package com.aizuda.snailjob.server.openapi.job.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author: shuguang.zhang
 * @date : 2023-10-12 10:23
 * @since : 2.4.0
 */
@Mapper
public interface JobBatchResponseConverter {

    JobBatchResponseConverter INSTANCE = Mappers.getMapper(JobBatchResponseConverter.class);

}
