package com.aizuda.easy.retry.server.web.service.convert;

import com.aizuda.easy.retry.server.web.model.response.DashboardCardResponseVO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardCardResponseDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author zhengweilin
 * @version 1.0.0
 * @date 2023/11/24
 */
@Mapper
public interface RetrySummaryResponseVOConverter {

    RetrySummaryResponseVOConverter INSTANCE = Mappers.getMapper(RetrySummaryResponseVOConverter.class);

    DashboardCardResponseVO.RetryTask toRetryTask(DashboardCardResponseDO.RetryTask retryTask);
}
