package com.aizuda.easy.retry.server.web.service.convert;

import com.aizuda.easy.retry.server.web.model.response.NamespaceResponseVO;
import com.aizuda.easy.retry.template.datasource.persistence.po.Namespace;
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
