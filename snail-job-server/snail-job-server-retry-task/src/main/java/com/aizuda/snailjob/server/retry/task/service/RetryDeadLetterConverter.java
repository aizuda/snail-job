package com.aizuda.snailjob.server.retry.task.service;

import com.aizuda.snailjob.template.datasource.persistence.po.RetryDeadLetter;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2023-07-25 12:35
 * @since 2.0.3
 */
@Mapper
public interface RetryDeadLetterConverter {

    RetryDeadLetterConverter INSTANCE = Mappers.getMapper(RetryDeadLetterConverter.class);

    @Named("ignoreId")
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createDt", ignore = true)
    })
    RetryDeadLetter toRetryDeadLetter(Retry retryTasks);

    @IterableMapping(qualifiedByName = "ignoreId")
    List<RetryDeadLetter> toRetryDeadLetter(List<Retry> retries);

}
