package com.aizuda.snailjob.server.web.service.convert;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.web.model.response.SceneConfigResponseVO;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2022-03-03 11:14
 */
@Mapper
public interface SceneConfigResponseVOConverter {

    SceneConfigResponseVOConverter INSTANCE = Mappers.getMapper(SceneConfigResponseVOConverter.class);

    List<SceneConfigResponseVO> convertList(List<RetrySceneConfig> retrySceneConfigs);

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(SceneConfigResponseVOConverter.toNotifyIds(retrySceneConfig.getNotifyIds()))")
    })
    SceneConfigResponseVO convert(RetrySceneConfig retrySceneConfig);

    static Set<Long> toNotifyIds(String notifyIds) {
        if (StrUtil.isBlank(notifyIds)) {
            return new HashSet<>();
        }

        return new HashSet<>(JsonUtil.parseList(notifyIds, Long.class));
    }
}
