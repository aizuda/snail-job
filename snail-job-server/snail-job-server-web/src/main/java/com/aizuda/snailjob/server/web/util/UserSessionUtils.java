package com.aizuda.snailjob.server.web.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
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
     * 1. 普通用户:
     *    1.1 查询条件为空, 返回用户的组权限
     *    1.2 查询条件不为空，返回用户的组权限与查询条件交集
     * 2. 管理员:
     *    2.1 查询条件为空, 返回空
     *    2.2 查询条件不为空, 返回查询条件组名
     *
     * @param groupNameQuery 组名查询条件
     * @return 用户组查询集合
     */
    public static List<String> getGroupNames(String groupNameQuery) {
        UserSessionVO userSessionVO = currentUserSession();
        if (userSessionVO.isUser()) { // 普通用户
            List<String> groupNames = userSessionVO.getGroupNames();
            if (CollUtil.isNotEmpty(groupNames)) {
                if (StrUtil.isNotBlank(groupNameQuery)) {
                    if (groupNames.contains(groupNameQuery)) {
                        return Lists.newArrayList(groupNameQuery);
                    }
                } else {
                    return groupNames;
                }
            }
        } else { // 管理员
            if (StrUtil.isNotBlank(groupNameQuery)) {
                return Lists.newArrayList(groupNameQuery);
            }
        }

        return Collections.emptyList();
    }
}
