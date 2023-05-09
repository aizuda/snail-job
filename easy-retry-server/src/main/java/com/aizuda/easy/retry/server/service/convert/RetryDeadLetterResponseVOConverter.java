package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryDeadLetter;
import com.aizuda.easy.retry.server.web.model.response.RetryDeadLetterResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-28 15:29
 */
@Mapper
public interface RetryDeadLetterResponseVOConverter {

    RetryDeadLetterResponseVOConverter INSTANCE = Mappers.getMapper(RetryDeadLetterResponseVOConverter.class);

    RetryDeadLetterResponseVO convert(RetryDeadLetter retryDeadLetter);

    List<RetryDeadLetterResponseVO> batchConvert(List<RetryDeadLetter> retryDeadLetters);
}
