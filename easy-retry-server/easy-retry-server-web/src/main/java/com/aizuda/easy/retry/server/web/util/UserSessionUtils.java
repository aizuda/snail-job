package com.aizuda.easy.retry.server.web.util;

import com.aizuda.easy.retry.server.web.model.request.UserSessionVO;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

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
}
