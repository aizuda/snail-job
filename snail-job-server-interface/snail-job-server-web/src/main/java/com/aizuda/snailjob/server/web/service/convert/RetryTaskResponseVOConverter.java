package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.web.model.response.RetryResponseVO;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author opensnail
 * @date 2022-02-27
 * @since 2.0
 */
@Mapper
public interface RetryTaskResponseVOConverter {

    RetryTaskResponseVOConverter INSTANCE = Mappers.getMapper(RetryTaskResponseVOConverter.class);

    @Mappings({
            @Mapping(target = "nextTriggerAt", expression = "java(RetryTaskResponseVOConverter.toLocalDateTime(retry.getNextTriggerAt()))")
    })
    RetryResponseVO convert(Retry retry);

    List<RetryResponseVO> convertList(List<Retry> retries);

    static LocalDateTime toLocalDateTime(Long nextTriggerAt) {
        if (Objects.isNull(nextTriggerAt) || nextTriggerAt == 0) {
            return null;
        }

        return DateUtils.toLocalDateTime(nextTriggerAt);
    }
}
