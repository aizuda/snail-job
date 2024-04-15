package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.server.web.model.request.NotifyConfigRequestVO;
import com.aizuda.snail.job.template.datasource.persistence.po.NotifyConfig;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author: opensnail
 * @date : 2021-11-26 13:43
 */
@Mapper
public interface NotifyConfigConverter {

    NotifyConfigConverter INSTANCE = Mappers.getMapper(NotifyConfigConverter.class);

    NotifyConfig toNotifyConfig(NotifyConfigRequestVO notifyConfigVO);
}
