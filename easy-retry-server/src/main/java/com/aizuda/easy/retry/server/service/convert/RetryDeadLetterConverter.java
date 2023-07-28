package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryDeadLetter;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
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

    @Mappings({
        @Mapping(source = "id", target = "id", ignore = true),
        @Mapping(source = "createDt", target = "createDt", ignore = true)
    })
    List<RetryDeadLetter> toRetryDeadLetter(List<RetryTask> retryTasks);

}
