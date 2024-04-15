package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.server.web.model.response.SceneConfigResponseVO;
import com.aizuda.snail.job.template.datasource.persistence.po.SceneConfig;
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

    SceneConfigResponseVO convert(SceneConfig sceneConfig);

    List<SceneConfigResponseVO> batchConvert(List<SceneConfig> sceneConfigs);
}
