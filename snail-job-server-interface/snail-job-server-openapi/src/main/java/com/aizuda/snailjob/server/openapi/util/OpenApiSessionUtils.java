package com.aizuda.snailjob.server.openapi.util;

import com.aizuda.snailjob.common.core.enums.HeadersEnum;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-05
 */
public final class OpenApiSessionUtils {

    public static String getNamespaceId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
       return request.getHeader(HeadersEnum.NAMESPACE.getKey());
    }

    public static String getGroupName() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request.getHeader(HeadersEnum.GROUP_NAME.getKey());
    }

}
