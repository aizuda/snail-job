package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.server.web.model.response.RetryResponseVO;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author opensnail
 * @date 2022-02-27
 * @since 2.0
 */
@Mapper
public interface RetryTaskResponseVOConverter {

    RetryTaskResponseVOConverter INSTANCE = Mappers.getMapper(RetryTaskResponseVOConverter.class);

    RetryResponseVO convert(Retry retry);

    List<RetryResponseVO> convertList(List<Retry> retries);
}
