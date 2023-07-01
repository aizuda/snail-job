package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.web.model.response.ServerNodeResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-06-06
 * @since 2.0
 */
@Mapper
public interface ServerNodeResponseVOConverter {

    ServerNodeResponseVOConverter INSTANCE = Mappers.getMapper(ServerNodeResponseVOConverter.class);

    List<ServerNodeResponseVO> toServerNodeResponseVO(List<ServerNode> serverNodes);

}
