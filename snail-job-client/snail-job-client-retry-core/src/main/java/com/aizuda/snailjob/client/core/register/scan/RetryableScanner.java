package com.aizuda.snailjob.client.core.register.scan;

import com.aizuda.snailjob.client.core.IdempotentIdGenerate;
import com.aizuda.snailjob.client.core.Scanner;
import com.aizuda.snailjob.client.core.annotation.Retryable;
import com.aizuda.snailjob.client.core.callback.RetryCompleteCallback;
import com.aizuda.snailjob.client.core.retryer.RetryType;
import com.aizuda.snailjob.client.core.retryer.RetryerInfo;
import com.aizuda.snailjob.client.core.strategy.ExecutorMethod;
import com.aizuda.snailjob.common.log.SnailJobLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author: opensnail
 * @date : 2022-03-03 16:55
 */
@Component
@Slf4j
public class RetryableScanner implements Scanner, ApplicationContextAware {

    public ApplicationContext applicationContext;

    @Override
    public List<RetryerInfo> doScan() {
        return scanRetryAbleMethod();
    }

    private List<RetryerInfo> scanRetryAbleMethod() {

        List<RetryerInfo> retryerInfoList = new ArrayList<>();
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanDefinitionName);

            Map<Method, Retryable> annotatedMethods = null;
            try {
                annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(),
                        (MethodIntrospector.MetadataLookup<Retryable>) method -> AnnotatedElementUtils
                                .findMergedAnnotation(method, Retryable.class));
            } catch (Throwable ex) {
                SnailJobLog.LOCAL.error("{}重试信息加载报错：{}", beanDefinitionName, ex);
            }
            if (annotatedMethods == null || annotatedMethods.isEmpty()) {
                continue;
            }

            for (Map.Entry<Method, Retryable> methodEntry : annotatedMethods.entrySet()) {
                Method executeMethod = methodEntry.getKey();
                Retryable retryable = methodEntry.getValue();
                RetryerInfo retryableRegistrarContext = resolvingRetryable(retryable, bean, executeMethod);
                retryerInfoList.add(retryableRegistrarContext);
            }
        }

        return retryerInfoList;
    }

    private RetryerInfo resolvingRetryable(Retryable retryable, Object executor, Method executorMethodName) {

        //异常校验处理
        Class<? extends Throwable>[] include = retryable.include();
        Class<? extends Throwable>[] exclude = retryable.exclude();

        Class executorNotProxy = AopProxyUtils.ultimateTargetClass(executor);
        String executorClassName = executorNotProxy.getName();
        Class<? extends IdempotentIdGenerate> idempotentIdGenerate = retryable.idempotentId();
        String bizNo = retryable.bizNo();
        RetryType retryType = retryable.retryStrategy();
        int localTimes = retryable.localTimes();
        int localInterval = retryable.localInterval();
        Class<? extends ExecutorMethod> retryMethod = retryable.retryMethod();
        boolean throwException = retryable.isThrowException();
        Class<? extends RetryCompleteCallback> retryCompleteCallback = retryable.retryCompleteCallback();
        boolean async = retryable.async();
        long timeout = retryable.timeout();
        TimeUnit unit = retryable.unit();

        return new RetryerInfo(retryable.scene(),
                executorClassName,
                new HashSet<>(Arrays.asList(include)),
                new HashSet<>(Arrays.asList(exclude)),
                executor,
                executorMethodName,
                retryType,
                localTimes,
                localInterval,
                idempotentIdGenerate,
                bizNo,
                retryMethod,
                throwException,
                retryCompleteCallback,
                async,
                Boolean.FALSE, // 基于注解的上报不允许强制上报
                timeout,
                unit
        );
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
