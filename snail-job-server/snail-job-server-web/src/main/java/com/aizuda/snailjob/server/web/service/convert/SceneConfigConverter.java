package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.server.web.model.request.SceneConfigRequestVO;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author: opensnail
 * @date : 2021-11-26 13:49
 */
@Mapper
public interface SceneConfigConverter {

    SceneConfigConverter INSTANCE = Mappers.getMapper(SceneConfigConverter.class);

    RetrySceneConfig convert(SceneConfigRequestVO requestVO);

}
