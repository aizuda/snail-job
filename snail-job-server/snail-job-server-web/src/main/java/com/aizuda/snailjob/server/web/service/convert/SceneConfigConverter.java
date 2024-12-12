package com.aizuda.snailjob.server.web.service.convert;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.web.model.request.SceneConfigRequestVO;
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
 * @date : 2021-11-26 13:49
 */
@Mapper
public interface SceneConfigConverter {

    SceneConfigConverter INSTANCE = Mappers.getMapper(SceneConfigConverter.class);

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(SceneConfigConverter.toNotifyIdsStr(requestVO.getNotifyIds()))")
    })
    RetrySceneConfig toRetrySceneConfig(SceneConfigRequestVO requestVO);

    List<RetrySceneConfig> toRetrySceneConfigs(List<SceneConfigRequestVO> requestVOs);

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(SceneConfigConverter.toNotifyIds(requestVOs.getNotifyIds()))")
    })
    List<SceneConfigRequestVO> toSceneConfigRequestVOs(List<RetrySceneConfig> requestVOs);

    static Set<Long> toNotifyIds(String notifyIds) {
        if (StrUtil.isBlank(notifyIds)) {
            return new HashSet<>();
        }

        return new HashSet<>(JsonUtil.parseList(notifyIds, Long.class));
    }

    static String toNotifyIdsStr(Set<Long> notifyIds) {
        if (CollUtil.isEmpty(notifyIds)) {
            return null;
        }

        return JsonUtil.toJsonString(notifyIds);
    }
}
