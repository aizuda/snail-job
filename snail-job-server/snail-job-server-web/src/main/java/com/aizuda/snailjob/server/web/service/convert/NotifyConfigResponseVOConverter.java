package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.snailjob.server.web.model.response.NotifyConfigResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-03-03 11:20
 */
@Mapper
public interface NotifyConfigResponseVOConverter {

    NotifyConfigResponseVOConverter INSTANCE = Mappers.getMapper(NotifyConfigResponseVOConverter.class);

    NotifyConfigResponseVO convert(NotifyConfig notifyConfig);

    List<NotifyConfigResponseVO> batchConvert(List<NotifyConfig> notifyConfigs);
}
