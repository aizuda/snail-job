package com.aizuda.easy.retry.client.core.intercepter;

import com.aizuda.easy.retry.client.core.intercepter.RetrySiteSnapshot.EnumStage;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.EasyRetryHeaders;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * 服务间调用传递请求头和响应头
 *
 * @author: www.byteblogs.com
 * @date : 2022-04-18 09:19
 * @since 1.0.0
 */
@Aspect
@Component
@Slf4j
public class HeaderAspect {

    public void before() {
        if (skip()) {
            return;
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String xRetry = attributes.getRequest().getHeader(SystemConstants.EASY_RETRY_HEAD_KEY);
        if (Objects.nonNull(xRetry)) {
            // 标记进入方法的时间
            RetrySiteSnapshot.setEntryMethodTime(System.currentTimeMillis());

            LogUtils.info(log, "easy-retry request header :[{}]", xRetry);
            RetrySiteSnapshot.setRetryHeader(JsonUtil.parseObject(xRetry, EasyRetryHeaders.class));
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
        response.addHeader(SystemConstants.EASY_RETRY_STATUS_CODE_KEY, RetrySiteSnapshot.getRetryStatusCode());

        if (skip()) {
            return;
        }

        // 服务端重试的在com.aizuda.easy.retry.client.core.client.RetryEndPoint.dispatch 中进行清除threadLocal
        if (Objects.nonNull(RetrySiteSnapshot.getStage()) && RetrySiteSnapshot.EnumStage.REMOTE.getStage() == RetrySiteSnapshot.getStage()) {
            return;
        }

        // 这里清除是为了,非服务端直接触发的节点，需要清除 threadLocal里面的标记
        RetrySiteSnapshot.removeRetryHeader();
        RetrySiteSnapshot.removeRetryStatusCode();
        RetrySiteSnapshot.removeEntryMethodTime();

    }

    /**
     * 本地重试不执行afterReturning和before方法，避免header传递失效
     *
     * @return
     */
    private boolean skip() {

        Integer stage = RetrySiteSnapshot.getStage();
        if (Objects.nonNull(stage) && EnumStage.LOCAL.getStage() == RetrySiteSnapshot.getStage()) {
            return true;
        }

        return false;
    }
}
