package com.x.retry.client.core.intercepter;

import com.x.retry.common.core.constant.SystemConstants;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.common.core.model.XRetryHeaders;
import com.x.retry.common.core.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 请求头和响应头传递
 *
 * @author: www.byteblogs.com
 * @date : 2022-04-18 09:19
 */
@Aspect
@Component
@Slf4j
public class HeaderAspect {

    public void before(){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String xRetry = attributes.getRequest().getHeader(SystemConstants.X_RETRY_HEAD_KEY);
        if (Objects.nonNull(xRetry)) {
            // 标记进入方法的时间
            RetrySiteSnapshot.setEntryMethodTime(System.currentTimeMillis());

            LogUtils.info(log, "x-retry 拦截器 xRetry:[{}]", xRetry);
            RetrySiteSnapshot.setRetryHeader(JsonUtil.parseObject(xRetry, XRetryHeaders.class));
        }
    }

    @Around(value = "@within(org.springframework.web.bind.annotation.RestController)")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        before();

        Throwable throwable = null;
        Object result = null;
        try {
            result = point.proceed();
        } catch (Throwable t) {
            throwable = t;
        } finally {
            afterReturning();
        }

        if (throwable != null) {
            throw throwable;
        } else {
            return result;
        }
    }

    public void afterReturning() {

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = attributes.getResponse();
        response.addHeader(SystemConstants.X_RETRY_STATUS_CODE_KEY, RetrySiteSnapshot.getRetryStatusCode());

        // 服务端重试的在com.x.retry.client.core.client.RetryEndPoint 中进行清除threadLocal
        if (Objects.nonNull(RetrySiteSnapshot.getStage()) && RetrySiteSnapshot.EnumStage.REMOTE.getStage() == RetrySiteSnapshot.getStage()) {
            return;
        }

        // 这里清除是为了,非服务端直接触发的节点，需要清除 threadLocal里面的标记
        RetrySiteSnapshot.removeRetryHeader();
        RetrySiteSnapshot.removeRetryStatusCode();
        RetrySiteSnapshot.removeEntryMethodTime();

    }
}
