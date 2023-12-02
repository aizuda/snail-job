package com.aizuda.easy.retry.server.web.service.convert;

import com.aizuda.easy.retry.server.web.model.response.DashboardRetryLineResponseVO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardRetryLineResponseDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-08-04 22:55:04
 * @since 2.2.0
 */
@Mapper
public interface SceneQuantityRankResponseVOConverter {

    SceneQuantityRankResponseVOConverter INSTANCE = Mappers.getMapper(SceneQuantityRankResponseVOConverter.class);

    List<DashboardRetryLineResponseVO.Rank> toDashboardRetryLineResponseVORank(List<DashboardRetryLineResponseDO.Rank> rankList);
}
