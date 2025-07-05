package com.aizuda.snailjob.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author opensnail
 * @date 2022-03-05
 * @since 2.0
 */
@Data
public class SystemUserResponseVO {

    private Long id;

    private String username;

    private Integer role;

    private List<String> groupNameList;

    private List<NamespaceResponseVO> namespaceIds;

    private List<PermissionsResponseVO> permissions;

    private String token;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

    private String mode;

}
