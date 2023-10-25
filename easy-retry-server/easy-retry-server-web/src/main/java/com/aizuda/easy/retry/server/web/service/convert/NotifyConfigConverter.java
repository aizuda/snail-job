package com.aizuda.easy.retry.server.web.service.convert;

import com.aizuda.easy.retry.server.web.model.request.NotifyConfigRequestVO;
import com.aizuda.easy.retry.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.easy.retry.server.web.model.request.GroupConfigRequestVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-26 13:43
 */
@Mapper
public interface NotifyConfigConverter {

    NotifyConfigConverter INSTANCE = Mappers.getMapper(NotifyConfigConverter.class);

    NotifyConfig toNotifyConfig(NotifyConfigRequestVO notifyConfigVO);
}
