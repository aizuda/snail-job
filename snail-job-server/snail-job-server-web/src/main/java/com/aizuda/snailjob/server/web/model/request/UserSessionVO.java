package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.web.annotation.RoleEnum;
import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * @author xiaowoniu
 * @date 2023-11-22 22:42:26
 * @since 2.5.0
 */
@Data
public final class UserSessionVO {

    private Long id;

    private String username;

    private Integer role;

    private String namespaceId;

    private List<String> groupNames;

    /**
     * 是否是普通用户
     */
    public boolean isUser() {
        return Objects.equals(this.role, RoleEnum.USER.getRoleId());
    }


}
