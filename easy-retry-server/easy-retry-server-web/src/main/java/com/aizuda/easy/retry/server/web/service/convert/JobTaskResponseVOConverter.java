package com.aizuda.easy.retry.server.web.service.convert;

import com.aizuda.easy.retry.server.web.model.response.JobTaskResponseVO;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-12 10:45
 * @since : 2.4.0
 */
@Mapper
public interface JobTaskResponseVOConverter {

    JobTaskResponseVOConverter INSTANCE = Mappers.getMapper(JobTaskResponseVOConverter.class);

    @Mappings(
            @Mapping(source = "id", target = "key")
    )
    List<JobTaskResponseVO> toJobTaskResponseVOs(List<JobTask> jobTasks);
}
