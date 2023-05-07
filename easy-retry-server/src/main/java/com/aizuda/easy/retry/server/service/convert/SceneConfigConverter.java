package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.persistence.mybatis.po.SceneConfig;
import com.aizuda.easy.retry.server.web.model.request.GroupConfigRequestVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-26 13:49
 */
@Mapper
public interface SceneConfigConverter {

    SceneConfigConverter INSTANCE = Mappers.getMapper(SceneConfigConverter.class);

    SceneConfig convert(GroupConfigRequestVO.SceneConfigVO sceneConfigVO);

    List<SceneConfig> batchConvert(List<GroupConfigRequestVO.SceneConfigVO> sceneConfigVOS);
}
