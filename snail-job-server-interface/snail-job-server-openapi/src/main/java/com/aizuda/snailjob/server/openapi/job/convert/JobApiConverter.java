package com.aizuda.snailjob.server.openapi.job.convert;

import com.aizuda.snailjob.server.common.vo.JobRequestVO;
import com.aizuda.snailjob.server.openapi.job.dto.JobRequestDTO;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
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

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(JobConverter.toNotifyIdsStr(jobRequestDTO.getNotifyIds()))"),
            @Mapping(target = "triggerInterval", expression = "java(JobConverter.toTriggerInterval(jobRequestDTO))")
    })
    Job convert(JobRequestDTO jobRequestDTO);
}

