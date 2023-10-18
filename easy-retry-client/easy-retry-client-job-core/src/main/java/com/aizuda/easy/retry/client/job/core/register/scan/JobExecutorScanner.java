package com.aizuda.easy.retry.client.job.core.register.scan;

import com.aizuda.easy.retry.client.job.core.IJobExecutor;
import com.aizuda.easy.retry.client.job.core.Scanner;
import com.aizuda.easy.retry.client.job.core.annotation.JobExecutor;
import com.aizuda.easy.retry.client.job.core.cache.JobExecutorInfoCache;
import com.aizuda.easy.retry.client.job.core.dto.JobArgs;
import com.aizuda.easy.retry.client.job.core.dto.JobContext;
import com.aizuda.easy.retry.client.job.core.dto.JobExecutorInfo;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-27 16:55
 */
@Component
@Slf4j
public class JobExecutorScanner implements Scanner, ApplicationContextAware {

    public ApplicationContext applicationContext;

    @Override
    public List<JobExecutorInfo> doScan() {
        return scanRetryAbleMethod();
    }

    private List<JobExecutorInfo> scanRetryAbleMethod() {

        List<JobExecutorInfo> retryerInfoList = new ArrayList<>();
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanDefinitionName);

            Map<Method, JobExecutor> annotatedMethods = null;
            try {
                annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(),
                        (MethodIntrospector.MetadataLookup<JobExecutor>) method -> AnnotatedElementUtils
                                .findMergedAnnotation(method, JobExecutor.class));
            } catch (Throwable ex) {
                LogUtils.error(log, "{} JobExecutor加载异常：{}", beanDefinitionName, ex);
            }

            String executorClassName = bean.getClass().getName();

            // 通过实现接口进行注册
            if (bean.getClass().isAssignableFrom(IJobExecutor.class)) {
                if (!JobExecutorInfoCache.isExisted(executorClassName)) {
                    retryerInfoList.add(new JobExecutorInfo(executorClassName, ReflectionUtils.findMethod(bean.getClass(), "jobExecute"), bean));
                }

            }

            // 扫描类的注解
            JobExecutor jobExecutor = bean.getClass().getAnnotation(JobExecutor.class);
            if (Objects.nonNull(jobExecutor)) {
                String executorName = jobExecutor.name();
                if (!JobExecutorInfoCache.isExisted(executorName)) {
                    JobExecutorInfo jobExecutorInfo =
                            new JobExecutorInfo(
                                    executorName,
                                    ReflectionUtils.findMethod(bean.getClass(), jobExecutor.method(), JobArgs.class),
                                    bean
                            );
                    retryerInfoList.add(jobExecutorInfo);
                }

            }

            if (CollectionUtils.isEmpty(annotatedMethods)) {
                continue;
            }

            // 扫描方法上的注解
            for (Map.Entry<Method, JobExecutor> methodEntry : annotatedMethods.entrySet()) {
                Method executeMethod = methodEntry.getKey();
                jobExecutor = methodEntry.getValue();
                if (JobExecutorInfoCache.isExisted(jobExecutor.name())) {
                    continue;
                }

                JobExecutorInfo jobExecutorInfo =
                        new JobExecutorInfo(
                                jobExecutor.name(),
                                executeMethod,
                                bean
                        );
                retryerInfoList.add(jobExecutorInfo);
            }
        }

        return retryerInfoList;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
