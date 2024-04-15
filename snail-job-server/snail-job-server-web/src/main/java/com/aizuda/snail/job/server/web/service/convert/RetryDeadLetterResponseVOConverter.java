package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.server.web.model.response.RetryDeadLetterResponseVO;
import com.aizuda.snail.job.template.datasource.persistence.po.RetryDeadLetter;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-02-28 15:29
 */
@Mapper
public interface RetryDeadLetterResponseVOConverter {

    RetryDeadLetterResponseVOConverter INSTANCE = Mappers.getMapper(RetryDeadLetterResponseVOConverter.class);

    RetryDeadLetterResponseVO convert(RetryDeadLetter retryDeadLetter);

    List<RetryDeadLetterResponseVO> batchConvert(List<RetryDeadLetter> retryDeadLetters);
}
