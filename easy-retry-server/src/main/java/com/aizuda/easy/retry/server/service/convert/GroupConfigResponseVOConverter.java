package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.persistence.mybatis.po.GroupConfig;
import com.aizuda.easy.retry.server.web.model.response.GroupConfigResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2022-02-27
 * @since 1.2.0
 */
@Mapper
public interface GroupConfigResponseVOConverter {

    GroupConfigResponseVOConverter INSTANCE = Mappers.getMapper(GroupConfigResponseVOConverter.class);

    GroupConfigResponseVO toGroupConfigResponseVO(GroupConfig groupConfig);

    List<GroupConfigResponseVO> toGroupConfigResponseVO(List<GroupConfig> groupConfigs);
}
