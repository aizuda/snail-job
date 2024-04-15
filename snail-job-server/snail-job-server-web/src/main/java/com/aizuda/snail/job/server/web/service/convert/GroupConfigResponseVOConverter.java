package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snail.job.server.web.model.response.GroupConfigResponseVO;
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

    GroupConfigResponseVO toGroupConfigResponseVO(GroupConfig groupConfig);

    List<GroupConfigResponseVO> toGroupConfigResponseVO(List<GroupConfig> groupConfigs);
}
