package com.aizuda.easy.retry.server.web.annotation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: byteblogs
 * @date: 2019/09/02 16:35
 */
public enum RoleEnum {

    /**
     * 普通用户
     */
    USER(1, "user"),

    /**
     * 管理员
     */
    ADMIN(2, "admin");

    private static final Map<Integer, RoleEnum> enumTypeMap = new HashMap<>();

    static {
        for (RoleEnum roleEnum : RoleEnum.values()) {
            enumTypeMap.put(roleEnum.getRoleId(), roleEnum);
        }
    }

    private final Integer roleId;

    private final String roleName;

    RoleEnum(Integer roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public static Map<Integer, RoleEnum> getEnumTypeMap() {
        return enumTypeMap;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

}
