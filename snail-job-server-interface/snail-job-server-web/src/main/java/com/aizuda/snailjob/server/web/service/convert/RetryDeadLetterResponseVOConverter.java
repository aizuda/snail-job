package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.server.web.model.response.RetryDeadLetterResponseVO;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryDeadLetter;
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

    List<RetryDeadLetterResponseVO> convertList(List<RetryDeadLetter> retryDeadLetters);
}
