package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.server.web.model.response.DashboardLineResponseVO;
import com.aizuda.snail.job.template.datasource.persistence.dataobject.DashboardLineResponseDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-08-04 22:51:56
 * @since 2.2.0
 */
@Mapper
public interface DispatchQuantityResponseVOConverter {

    DispatchQuantityResponseVOConverter INSTANCE = Mappers.getMapper(DispatchQuantityResponseVOConverter.class);

    List<DashboardLineResponseVO> toDashboardLineResponseVO(List<DashboardLineResponseDO> dashboardLineResponseDOList);
}
