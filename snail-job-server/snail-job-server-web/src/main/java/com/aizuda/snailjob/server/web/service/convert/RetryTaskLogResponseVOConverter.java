package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.snailjob.server.web.model.response.RetryTaskLogResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-02-28 09:19
 */
@Mapper
public interface RetryTaskLogResponseVOConverter {

    RetryTaskLogResponseVOConverter INSTANCE = Mappers.getMapper(RetryTaskLogResponseVOConverter.class);

    RetryTaskLogResponseVO convert(RetryTaskLog retryTaskLog);

    List<RetryTaskLogResponseVO> batchConvert(List<RetryTaskLog> retryTaskLogs);

}
