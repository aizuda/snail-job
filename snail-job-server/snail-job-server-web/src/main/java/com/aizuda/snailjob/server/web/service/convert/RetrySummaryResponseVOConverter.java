package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.server.web.model.response.DashboardCardResponseVO.RetryTask;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.DashboardCardResponseDO;
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
