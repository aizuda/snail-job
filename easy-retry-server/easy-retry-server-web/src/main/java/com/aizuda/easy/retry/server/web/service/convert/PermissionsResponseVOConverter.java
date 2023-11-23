package com.aizuda.easy.retry.server.web.service.convert;

import com.aizuda.easy.retry.server.web.model.response.PermissionsResponseVO;
import com.aizuda.easy.retry.template.datasource.persistence.po.SystemUserPermission;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: xiaowoniu
 * @date : 2023-11-23 14:04
 * @since : 2.5.0
 */
@Mapper
public interface PermissionsResponseVOConverter {

    PermissionsResponseVOConverter INSTANCE = Mappers.getMapper(PermissionsResponseVOConverter.class);

    List<PermissionsResponseVO> toPermissionsResponseVOs(List<SystemUserPermission> systemUserPermissionList);

}
