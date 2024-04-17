package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.server.web.model.response.NotifyRecipientResponseVO;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyRecipient;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author opensnail
 * @date 2024-04-17 22:00:41
 * @since sj_1.0.0
 */
@Mapper
public interface NotifyRecipientResponseVOConverter {

    NotifyRecipientResponseVOConverter INSTANCE = Mappers.getMapper(NotifyRecipientResponseVOConverter.class);

    List<NotifyRecipientResponseVO> toNotifyRecipientResponseVOs(List<NotifyRecipient> notifyRecipients);

}
