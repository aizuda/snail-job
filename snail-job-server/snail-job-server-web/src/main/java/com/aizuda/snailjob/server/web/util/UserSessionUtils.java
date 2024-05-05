package com.aizuda.snailjob.server.web.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
     * 判断当前用户是否具备组权限
     *
     * @param groupName 组名称
     * @return true, 具备权限，否则返回false
     */
    public static boolean hasGroupPermission(String groupName) {
        if (StrUtil.isBlank(groupName)) {
            return true;
        }

        UserSessionVO userSessionVO = currentUserSession();
        if (userSessionVO.isUser() && CollUtil.isNotEmpty(userSessionVO.getGroupNames())) {
            return userSessionVO.getGroupNames().contains(groupName);
        }

        return false;
    }
}
