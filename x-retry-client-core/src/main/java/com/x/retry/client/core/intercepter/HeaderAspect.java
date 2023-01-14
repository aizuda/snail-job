package com.x.retry.client.core.intercepter;

import com.x.retry.common.core.constant.SystemConstants;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.common.core.model.XRetryHeaders;
import com.x.retry.common.core.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author: shuguang.zhang
 * @date : 2022-04-18 09:19
 */

@Aspect
@Component
@Slf4j
public class HeaderAspect {

    @Before(value = "@within(org.springframework.web.bind.annotation.RestController)")
    public void before(JoinPoint joinPoint){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String xRetry = attributes.getRequest().getHeader(SystemConstants.X_RETRY_HEAD_KEY);
        if (Objects.nonNull(xRetry)) {
            // 标记进入方法的时间
            RetrySiteSnapshot.setEntryMethodTime(System.currentTimeMillis());

            LogUtils.info("x-retry 拦截器 xRetry:[{}]", xRetry);
            RetrySiteSnapshot.setRetryHeader(JsonUtil.parseObject(xRetry, XRetryHeaders.class));
        }
    }

    @AfterReturning(pointcut = "@within(org.springframework.web.bind.annotation.RestController)", returning = "o")
    public void afterReturning(Object o) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = attributes.getResponse();
        response.addHeader(SystemConstants.X_RETRY_STATUS_CODE_KEY, RetrySiteSnapshot.getRetryStatusCode());

        RetrySiteSnapshot.removeRetryHeader();
        RetrySiteSnapshot.removeRetryStatusCode();
        RetrySiteSnapshot.removeEntryMethodTime();

    }
}
