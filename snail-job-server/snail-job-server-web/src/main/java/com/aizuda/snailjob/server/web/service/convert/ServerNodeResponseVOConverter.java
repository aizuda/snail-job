package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
import com.aizuda.snailjob.server.web.model.response.ServerNodeResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-06-06
 * @since 2.0
 */
@Mapper
public interface ServerNodeResponseVOConverter {

    ServerNodeResponseVOConverter INSTANCE = Mappers.getMapper(ServerNodeResponseVOConverter.class);

    List<ServerNodeResponseVO> toServerNodeResponseVO(List<ServerNode> serverNodes);

}
