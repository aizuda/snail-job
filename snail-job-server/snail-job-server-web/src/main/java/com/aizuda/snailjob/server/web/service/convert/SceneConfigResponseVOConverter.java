package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.server.web.model.response.SceneConfigResponseVO;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-03-03 11:14
 */
@Mapper
public interface SceneConfigResponseVOConverter {

    SceneConfigResponseVOConverter INSTANCE = Mappers.getMapper(SceneConfigResponseVOConverter.class);

    SceneConfigResponseVO convert(RetrySceneConfig retrySceneConfig);

    List<SceneConfigResponseVO> convertList(List<RetrySceneConfig> retrySceneConfigs);
}
