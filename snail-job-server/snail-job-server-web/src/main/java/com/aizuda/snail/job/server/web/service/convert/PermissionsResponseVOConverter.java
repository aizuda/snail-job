package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.server.web.model.response.PermissionsResponseVO;
import com.aizuda.snail.job.template.datasource.persistence.po.SystemUserPermission;
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
