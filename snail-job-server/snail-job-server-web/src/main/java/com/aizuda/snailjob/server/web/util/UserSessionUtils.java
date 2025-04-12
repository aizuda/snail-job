package com.aizuda.snailjob.server.web.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.exception.SnailJobAuthenticationException;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;

/**
 * @author opensnail
 * @date 2023-11-22 23:14:53
 * @since 2.4.0
 */
public final class UserSessionUtils {

    public static UserSessionVO currentUserSession() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return (UserSessionVO) request.getAttribute("currentUser");
    }

    /**
     * 计算用户组权限及组名查询条件的组合结果
     * <p>
     * 1. 管理员:
     * 1.1 查询条件为空, 返回空
     * 1.2 查询条件不为空, 返回查询条件组名
     * <p>
     * 2. 普通用户:
     * 2.1 查询条件为空, 返回用户的组权限
     * 2.2 查询条件不为空，返回用户的组权限与查询条件交集
     *
     * @param groupNameQuery 组名查询条件
     * @return 用户组查询集合
     */
    public static List<String> getGroupNames(String groupNameQuery) {
        UserSessionVO userSessionVO = currentUserSession();
        if (userSessionVO.isAdmin()) {
            // 若是管理员且存在查询条件
            if (StrUtil.isNotBlank(groupNameQuery)) {
                return Lists.newArrayList(groupNameQuery);
            }
            return Collections.emptyList();
        } else {
            List<String> groupNames = userSessionVO.getGroupNames();
            Assert.notEmpty(groupNames, () -> new SnailJobAuthenticationException("Normal user group permissions are empty"));
            // 若是普通用户且权限包括查询条件
            if (StrUtil.isNotBlank(groupNameQuery) && groupNames.contains(groupNameQuery)) {
                return Lists.newArrayList(groupNameQuery);
            }
            return groupNames;
        }
    }
}
