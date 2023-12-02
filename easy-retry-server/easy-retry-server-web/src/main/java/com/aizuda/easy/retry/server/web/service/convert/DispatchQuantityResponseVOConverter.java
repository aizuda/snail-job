package com.aizuda.easy.retry.server.web.service.convert;

import com.aizuda.easy.retry.server.web.model.response.DashboardRetryLinkeResponseVO;
import com.aizuda.easy.retry.server.web.model.response.DispatchQuantityResponseVO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardRetryLinkeResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DispatchQuantityResponseDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-08-04 22:51:56
 * @since 2.2.0
 */
@Mapper
public interface DispatchQuantityResponseVOConverter {

    DispatchQuantityResponseVOConverter INSTANCE = Mappers.getMapper(DispatchQuantityResponseVOConverter.class);

    List<DashboardRetryLinkeResponseVO> toDashboardRetryLinkeResponseVO(List<DashboardRetryLinkeResponseDO> dashboardRetryLinkeResponseDOList);

    List<DispatchQuantityResponseVO> toDispatchQuantityResponseVO(List<DispatchQuantityResponseDO> dispatchQuantityResponseDOList);
}
