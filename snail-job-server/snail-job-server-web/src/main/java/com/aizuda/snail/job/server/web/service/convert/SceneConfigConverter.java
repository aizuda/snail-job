package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.server.web.model.request.SceneConfigRequestVO;
import com.aizuda.snail.job.template.datasource.persistence.po.SceneConfig;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author: opensnail
 * @date : 2021-11-26 13:49
 */
@Mapper
public interface SceneConfigConverter {

    SceneConfigConverter INSTANCE = Mappers.getMapper(SceneConfigConverter.class);

    SceneConfig toSceneConfigRequestVO(SceneConfigRequestVO requestVO);

}
