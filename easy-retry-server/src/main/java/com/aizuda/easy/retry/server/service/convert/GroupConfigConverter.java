package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.persistence.mybatis.po.GroupConfig;
import com.aizuda.easy.retry.server.web.model.request.GroupConfigRequestVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * GroupConfigVO装换为GroupConfig
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-26 13:55
 */
@Mapper
public interface GroupConfigConverter {

    GroupConfigConverter INSTANCE = Mappers.getMapper(GroupConfigConverter.class);

    GroupConfig convert(GroupConfigRequestVO groupConfigRequestVO);

    List<GroupConfig> batchConvert(List<GroupConfigRequestVO> groupConfigRequestVOS);
}
