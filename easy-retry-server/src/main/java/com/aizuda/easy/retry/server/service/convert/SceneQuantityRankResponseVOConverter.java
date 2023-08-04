package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.web.model.response.SceneQuantityRankResponseVO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DispatchQuantityResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.SceneQuantityRankResponseDO;
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

    List<SceneQuantityRankResponseVO> toSceneQuantityRankResponseVO(List<SceneQuantityRankResponseDO> dispatchQuantityResponseDOS);
}
