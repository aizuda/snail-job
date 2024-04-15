package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.server.web.model.response.NamespaceResponseVO;
import com.aizuda.snail.job.template.datasource.persistence.po.Namespace;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: xiaowoniu
 * @date : 2023-11-21 16:20
 * @since : 2.5.0
 */
@Mapper
public interface NamespaceResponseVOConverter {

    NamespaceResponseVOConverter INSTANCE = Mappers.getMapper(NamespaceResponseVOConverter.class);

    List<NamespaceResponseVO> toNamespaceResponseVOs(List<Namespace> namespaces);
}
