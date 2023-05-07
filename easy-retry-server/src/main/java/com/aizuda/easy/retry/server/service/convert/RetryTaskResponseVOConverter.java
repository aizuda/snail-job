package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.web.model.response.RetryTaskResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2022-02-27
 * @since 2.0
 */
@Mapper
public interface RetryTaskResponseVOConverter {

    RetryTaskResponseVOConverter INSTANCE = Mappers.getMapper(RetryTaskResponseVOConverter.class);

    RetryTaskResponseVO toRetryTaskResponseVO(RetryTask retryTask);

    List<RetryTaskResponseVO> toRetryTaskResponseVO(List<RetryTask> retryTasks);
}
