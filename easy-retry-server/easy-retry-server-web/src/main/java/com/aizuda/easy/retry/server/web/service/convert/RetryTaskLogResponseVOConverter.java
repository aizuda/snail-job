package com.aizuda.easy.retry.server.web.service.convert;

import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLogMessage;
import com.aizuda.easy.retry.server.web.model.response.RetryTaskLogMessageResponseVO;
import com.aizuda.easy.retry.server.web.model.response.RetryTaskLogResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-28 09:19
 */
@Mapper
public interface RetryTaskLogResponseVOConverter {

    RetryTaskLogResponseVOConverter INSTANCE = Mappers.getMapper(RetryTaskLogResponseVOConverter.class);

    RetryTaskLogResponseVO convert(RetryTaskLog retryTaskLog);

    List<RetryTaskLogResponseVO> batchConvert(List<RetryTaskLog> retryTaskLogs);

}
