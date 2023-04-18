package com.aizuda.easy.retry.server.web.interceptor;

import com.aizuda.easy.retry.server.persistence.mybatis.po.SystemUser;
import com.aizuda.easy.retry.server.web.annotation.LoginUser;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * @author www.byteblogs.com
 * @date 2022-03-06
 * @since 2.0
 */
@Component
public class LoginUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(SystemUser.class)
                && parameter.hasParameterAnnotation(LoginUser.class);
    }
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        SystemUser userInfo = (SystemUser) webRequest.getAttribute("currentUser", RequestAttributes.SCOPE_REQUEST);
        if (userInfo != null) {
            return userInfo;
        }
        throw new MissingServletRequestPartException("currentUser");
    }
}
