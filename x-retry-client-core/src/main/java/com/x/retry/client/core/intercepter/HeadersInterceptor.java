package com.x.retry.client.core.intercepter;

import com.x.retry.common.core.constant.SystemConstants;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.common.core.model.XRetryHeaders;
import com.x.retry.common.core.util.JsonUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @Author:byteblogs
 * @Date:2018/09/27 12:52
 */
@Component
public class HeadersInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {
        String xRetry = httpServletRequest.getHeader(SystemConstants.X_RETRY_HEAD);
        if (Objects.nonNull(xRetry)) {
            LogUtils.info("x-retry 拦截器 xRetry:[{}]", xRetry);
            RetrySiteSnapshot.setRetryHeader(JsonUtil.parseObject(xRetry, XRetryHeaders.class));
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Object o, Exception e) throws Exception {
    }
}
