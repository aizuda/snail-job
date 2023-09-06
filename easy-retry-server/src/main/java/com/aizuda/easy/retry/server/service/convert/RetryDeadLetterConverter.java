package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.template.datasource.persistence.po.RetryDeadLetter;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: www.byteblogs.com
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
    RetryDeadLetter toRetryDeadLetter(RetryTask retryTasks);

    @IterableMapping(qualifiedByName = "ignoreId")
    List<RetryDeadLetter> toRetryDeadLetter(List<RetryTask> retryTasks);

}
