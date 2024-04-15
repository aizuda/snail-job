package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.server.web.model.response.DashboardCardResponseVO.RetryTask;
import com.aizuda.snail.job.template.datasource.persistence.dataobject.DashboardCardResponseDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author zhengweilin
 * @version 2.6.0
 * @date 2023/11/24
 */
@Mapper
public interface RetrySummaryResponseVOConverter {

    RetrySummaryResponseVOConverter INSTANCE = Mappers.getMapper(RetrySummaryResponseVOConverter.class);

    RetryTask toRetryTask(DashboardCardResponseDO.RetryTask retryTask);
}
