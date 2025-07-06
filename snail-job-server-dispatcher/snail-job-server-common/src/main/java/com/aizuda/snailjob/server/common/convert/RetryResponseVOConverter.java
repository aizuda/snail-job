package com.aizuda.snailjob.server.common.convert;

import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.common.vo.RetryResponseVO;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Objects;

@Mapper
public interface RetryResponseVOConverter {
    RetryResponseVOConverter INSTANCE = Mappers.getMapper(RetryResponseVOConverter.class);

    @Mappings({
            @Mapping(target = "nextTriggerAt", expression = "java(RetryResponseVOConverter.toLocalDateTime(retry.getNextTriggerAt()))"),
    })
    RetryResponseVO convert(Retry retry);

    static LocalDateTime toLocalDateTime(Long nextTriggerAt) {
        if (Objects.isNull(nextTriggerAt) || nextTriggerAt == 0) {
            return null;
        }
        return DateUtils.toLocalDateTime(nextTriggerAt);
    }
}
