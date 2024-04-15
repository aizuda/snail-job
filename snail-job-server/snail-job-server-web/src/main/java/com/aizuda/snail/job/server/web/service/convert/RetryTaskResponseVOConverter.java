package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.template.datasource.persistence.po.RetryTask;
import com.aizuda.snail.job.server.web.model.response.RetryTaskResponseVO;
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

    RetryTaskResponseVO toRetryTaskResponseVO(RetryTask retryTask);

    List<RetryTaskResponseVO> toRetryTaskResponseVO(List<RetryTask> retryTasks);
}
