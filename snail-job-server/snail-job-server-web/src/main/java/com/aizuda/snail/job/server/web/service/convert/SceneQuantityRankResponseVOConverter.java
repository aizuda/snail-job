package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.server.web.model.response.DashboardRetryLineResponseVO.Rank;
import com.aizuda.snail.job.template.datasource.persistence.dataobject.DashboardRetryLineResponseDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-08-04 22:55:04
 * @since 2.2.0
 */
@Mapper
public interface SceneQuantityRankResponseVOConverter {

    SceneQuantityRankResponseVOConverter INSTANCE = Mappers.getMapper(SceneQuantityRankResponseVOConverter.class);

    List<Rank> toDashboardRetryLineResponseVORank(List<DashboardRetryLineResponseDO.Rank> rankList);
}
