package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.persistence.mybatis.po.NotifyConfig;
import com.aizuda.easy.retry.server.web.model.response.NotifyConfigResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 11:20
 */
@Mapper
public interface NotifyConfigResponseVOConverter {

    NotifyConfigResponseVOConverter INSTANCE = Mappers.getMapper(NotifyConfigResponseVOConverter.class);

    NotifyConfigResponseVO convert(NotifyConfig notifyConfig);

    List<NotifyConfigResponseVO> batchConvert(List<NotifyConfig> notifyConfigs);
}
