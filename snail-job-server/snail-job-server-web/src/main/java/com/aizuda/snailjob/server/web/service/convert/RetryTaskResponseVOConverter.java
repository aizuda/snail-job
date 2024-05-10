package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.aizuda.snailjob.server.web.model.response.RetryTaskResponseVO;
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

    RetryTaskResponseVO convert(RetryTask retryTask);

    List<RetryTaskResponseVO> convertList(List<RetryTask> retryTasks);
}
