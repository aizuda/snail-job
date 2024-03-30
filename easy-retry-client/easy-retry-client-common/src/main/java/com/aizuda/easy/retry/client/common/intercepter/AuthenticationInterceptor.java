package com.aizuda.easy.retry.client.common.intercepter;

import com.aizuda.easy.retry.client.common.config.EasyRetryProperties;
import com.aizuda.easy.retry.client.common.exception.EasyRetryClientException;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Easy Retry 认证拦截器
 *
 * @author: xiaowoniu
 * @date : 2022-04-18 09:19
 * @since 3.2.0
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor {
    private final EasyRetryProperties easyRetryProperties;
    @Before(value = "@annotation(com.aizuda.easy.retry.client.common.annotation.Authentication) || @within(com.aizuda.easy.retry.client.common.annotation.Authentication)")
    public void easyRetryAuth() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String easyRetryAuth = attributes.getRequest().getHeader(SystemConstants.EASY_RETRY_AUTH_TOKEN);
        String configToken = Optional.ofNullable(easyRetryProperties.getToken()).orElse(SystemConstants.DEFAULT_TOKEN);
        if (!configToken.equals(easyRetryAuth)) {
            throw new EasyRetryClientException("认证失败.【请检查配置的Token是否正确】");
        }
    }

}
