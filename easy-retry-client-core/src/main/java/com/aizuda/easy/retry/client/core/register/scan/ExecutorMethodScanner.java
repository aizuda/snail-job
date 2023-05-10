package com.aizuda.easy.retry.client.core.register.scan;

import com.aizuda.easy.retry.client.core.IdempotentIdGenerate;
import com.aizuda.easy.retry.client.core.Scanner;
import com.aizuda.easy.retry.client.core.annotation.ExecutorMethodRegister;
import com.aizuda.easy.retry.client.core.callback.RetryCompleteCallback;
import com.aizuda.easy.retry.client.core.retryer.RetryType;
import com.aizuda.easy.retry.client.core.retryer.RetryerInfo;
import com.aizuda.easy.retry.client.core.strategy.ExecutorMethod;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 扫描手动注入重试方法
 *
 * @author: www.byteblogs.com
 * @date : 2023-05-10 11:10
 */
@Component
@Slf4j
public class ExecutorMethodScanner implements Scanner, ApplicationContextAware {

    public ApplicationContext applicationContext;

    @Override
    public List<RetryerInfo> doScan() {
        List<RetryerInfo> retryerInfoList = new ArrayList<>();
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);

        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanDefinitionName);
            ExecutorMethodRegister annotation = bean.getClass().getAnnotation(ExecutorMethodRegister.class);
            if (Objects.nonNull(annotation)) {
                RetryerInfo retryerInfo = resolvingRetryable(annotation, bean);
                Optional.ofNullable(retryerInfo).ifPresent(retryerInfoList::add);
            }
        }

        return retryerInfoList;
    }

    private RetryerInfo resolvingRetryable(ExecutorMethodRegister retryable, Object executor) {

        try {
            Class executorNotProxy = AopUtils.getTargetClass(executor);
            String executorClassName = executorNotProxy.getName();
            Class<? extends IdempotentIdGenerate> idempotentIdGenerate = retryable.idempotentId();
            Method executorMethodName = executorNotProxy.getMethod("doExecute", Object.class);
            Class<? extends RetryCompleteCallback> retryCompleteCallback = retryable.retryCompleteCallback();

            return new RetryerInfo(retryable.scene(),
                executorClassName,
                new HashSet<>(Collections.emptyList()),
                new HashSet<>(Collections.emptyList()),
                executor,
                executorMethodName,
                RetryType.ONLY_REMOTE,
                0,
                0,
                idempotentIdGenerate,
                StringUtils.EMPTY,
                (Class<? extends ExecutorMethod>) executor.getClass(),
                true,
                retryCompleteCallback
            );
        }catch (Exception e) {
            LogUtils.error(log, "{}重试信息加载报错：{}", executor.getClass().getName(), e);
        }

        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
