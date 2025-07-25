package com.aizuda.snailjob.server.service.convert;

import com.aizuda.snailjob.server.service.dto.RetryResponseBaseDTO;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

@Mapper
public interface RetryConverter {
    RetryConverter INSTANCE = Mappers.getMapper(RetryConverter.class);

    @Mappings({
            @Mapping(target = "nextTriggerAt", expression = "java(RetryResponseVOConverter.toLocalDateTime(retry.getNextTriggerAt()))"),
    })
    void toRetryResponseVO(Retry retry, @MappingTarget RetryResponseBaseDTO baseDTO);

    static LocalDateTime toLocalDateTime(Long nextTriggerAt) {
        return JobConverter.toLocalDateTime(nextTriggerAt);
    }
}
