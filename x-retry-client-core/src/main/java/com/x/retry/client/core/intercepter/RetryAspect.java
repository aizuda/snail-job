package com.x.retry.client.core.intercepter;

import cn.hutool.core.util.StrUtil;
import com.x.retry.client.core.annotation.Retryable;
import com.x.retry.client.core.exception.XRetryClientException;
import com.x.retry.client.core.retryer.RetryerResultContext;
import com.x.retry.client.core.strategy.RetryStrategy;
import com.x.retry.common.core.enums.RetryResultStatusEnum;
import com.x.retry.common.core.log.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 11:41
 */
@Aspect
@Component
@Slf4j
public class RetryAspect {

    @Autowired
    @Qualifier("localRetryStrategies")
    private RetryStrategy retryStrategy;

    @Around("@annotation(com.x.retry.client.core.annotation.Retryable)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        String traceId = UUID.randomUUID().toString();

        LogUtils.debug("进入 aop [{}]",traceId);
        Retryable retryable = getAnnotationParameter(point);
        String executorClassName = point.getTarget().getClass().getName();
        String methodEntrance = getMethodEntrance(retryable, executorClassName);
        if (StrUtil.isBlank(RetrySiteSnapshot.getMethodEntrance())) {
            RetrySiteSnapshot.setMethodEntrance(methodEntrance);
        }

        Throwable throwable = null;
        Object result = null;
        try {
            result = point.proceed();
        } catch (Throwable t) {
            throwable = t;
        } finally {

            if (RetrySiteSnapshot.isMethodEntrance(methodEntrance) && !RetrySiteSnapshot.isRunning() && Objects.nonNull(throwable)) {
                LogUtils.debug("开始进行重试 aop [{}]", traceId);
                try {
                    // 入口则开始处理重试
                    RetryerResultContext context = retryStrategy.openRetry(retryable.scene(), executorClassName, point.getArgs());
                    if (RetryResultStatusEnum.SUCCESS.getStatus().equals(context.getRetryResultStatusEnum().getStatus())) {
                        LogUtils.debug("aop 结果成功 traceId:[{}] result:[{}]", traceId, context.getResult());
                    }

                } catch (Exception e) {
                    LogUtils.error("重试组件处理异常，{}", e);
                    // TODO调用通知

                } finally {
                    RetrySiteSnapshot.removeAll();
                }
            }
        }

       LogUtils.debug("aop 结果处理 traceId:[{}] result:[{}] ", traceId, result, throwable);
        if (throwable != null) {
            throw throwable;
        } else {
            return result;
        }

    }

    public String getMethodEntrance(Retryable retryable, String executorClassName) {

        if (Objects.isNull(retryable)) {
            return StrUtil.EMPTY;
        }

        return retryable.scene().concat("_").concat(executorClassName);
    }

    private Retryable getAnnotationParameter(ProceedingJoinPoint point) {
        String methodName = point.getSignature().getName();
        Class<?> classTarget = point.getTarget().getClass();
        Class<?>[] par = ((MethodSignature) point.getSignature()).getParameterTypes();
        Method objMethod = null;
        try {
            objMethod = classTarget.getMethod(methodName, par);
        } catch (NoSuchMethodException e) {
            throw new XRetryClientException("注解配置异常：[{}}", methodName);
        }
        return objMethod.getAnnotation(Retryable.class);
    }
}
