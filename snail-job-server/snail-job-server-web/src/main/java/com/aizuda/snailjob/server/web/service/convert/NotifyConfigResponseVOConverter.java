package com.aizuda.snailjob.server.web.service.convert;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.snailjob.server.web.model.response.NotifyConfigResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2022-03-03 11:20
 */
@Mapper
public interface NotifyConfigResponseVOConverter {

    NotifyConfigResponseVOConverter INSTANCE = Mappers.getMapper(NotifyConfigResponseVOConverter.class);

    @Mappings({
        @Mapping(target = "recipientIds", expression = "java(NotifyConfigResponseVOConverter.toNotifyRecipientIds(notifyConfig.getRecipientIds()))")
    })
    NotifyConfigResponseVO convert(NotifyConfig notifyConfig);

    List<NotifyConfigResponseVO> convertList(List<NotifyConfig> notifyConfigs);

    static Set<Long> toNotifyRecipientIds(String notifyRecipientIdsStr) {
        if (StrUtil.isBlank(notifyRecipientIdsStr)) {
            return new HashSet<>();
        }

        return new HashSet<>(JsonUtil.parseList(notifyRecipientIdsStr, Long.class));
    }
}
