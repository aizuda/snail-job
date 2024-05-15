package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.web.model.request.NotifyConfigRequestVO;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * @author: opensnail
 * @date : 2021-11-26 13:43
 */
@Mapper
public interface NotifyConfigConverter {

    NotifyConfigConverter INSTANCE = Mappers.getMapper(NotifyConfigConverter.class);

    static String toNotifyRecipientIdsStr(Set<Long> notifyRecipientIds) {
        if (CollectionUtils.isEmpty(notifyRecipientIds)) {
            return null;
        }

        return JsonUtil.toJsonString(notifyRecipientIds);
    }

    @Mappings({
            @Mapping(target = "recipientIds", expression = "java(NotifyConfigConverter.toNotifyRecipientIdsStr(notifyConfigVO.getRecipientIds()))")
    })
    NotifyConfig convert(NotifyConfigRequestVO notifyConfigVO);
}
