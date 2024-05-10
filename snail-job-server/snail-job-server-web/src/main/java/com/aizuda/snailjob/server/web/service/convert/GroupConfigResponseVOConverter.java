package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.server.web.model.response.GroupConfigResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author opensnail
 * @date 2022-02-27
 * @since 1.2.0
 */
@Mapper
public interface GroupConfigResponseVOConverter {

    GroupConfigResponseVOConverter INSTANCE = Mappers.getMapper(GroupConfigResponseVOConverter.class);

    GroupConfigResponseVO convert(GroupConfig groupConfig);

    List<GroupConfigResponseVO> convertList(List<GroupConfig> groupConfigs);
}
