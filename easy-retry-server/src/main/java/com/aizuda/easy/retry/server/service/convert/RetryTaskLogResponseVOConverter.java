package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTaskLog;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTaskLogMessage;
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

    List<RetryTaskLogMessageResponseVO> toRetryTaskLogMessageResponseVO(List<RetryTaskLogMessage> retryTaskLogs);
}
