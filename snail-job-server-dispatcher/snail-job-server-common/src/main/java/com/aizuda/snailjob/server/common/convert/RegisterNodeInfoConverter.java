package com.aizuda.snailjob.server.common.convert;

import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author: opensnail
 * @date : 2023-06-09 11:07
 * @since 1.6.0
 */
@Mapper
public interface RegisterNodeInfoConverter {
    RegisterNodeInfoConverter INSTANCE = Mappers.getMapper(RegisterNodeInfoConverter.class);

    RegisterNodeInfo toRegisterNodeInfo(ServerNode serverNode);
}
