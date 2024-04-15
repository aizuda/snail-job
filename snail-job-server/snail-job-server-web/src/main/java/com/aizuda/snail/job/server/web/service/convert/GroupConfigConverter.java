package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.server.web.model.request.GroupConfigRequestVO;
import com.aizuda.snail.job.template.datasource.persistence.po.GroupConfig;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * GroupConfigVO装换为GroupConfig
 *
 * @author: opensnail
 * @date : 2021-11-26 13:55
 */
@Mapper
public interface GroupConfigConverter {

    GroupConfigConverter INSTANCE = Mappers.getMapper(GroupConfigConverter.class);

    GroupConfig convert(GroupConfigRequestVO groupConfigRequestVO);

    List<GroupConfig> batchConvert(List<GroupConfigRequestVO> groupConfigRequestVOS);
}
