package com.aizuda.snailjob.client.job.core.register.scan;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.client.job.core.IJobExecutor;
import com.aizuda.snailjob.client.job.core.Scanner;
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.annotation.MapExecutor;
import com.aizuda.snailjob.client.job.core.annotation.MergeReduceExecutor;
import com.aizuda.snailjob.client.job.core.annotation.ReduceExecutor;
import com.aizuda.snailjob.client.job.core.cache.JobExecutorInfoCache;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.job.core.dto.JobExecutorInfo;
import com.aizuda.snailjob.client.job.core.dto.MapArgs;
import com.aizuda.snailjob.client.job.core.dto.MergeReduceArgs;
import com.aizuda.snailjob.client.job.core.dto.ReduceArgs;
import com.aizuda.snailjob.common.log.SnailJobLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: opensnail
 * @date : 2023-09-27 16:55
 */
@Component
@Slf4j
public class JobExecutorScanner implements Scanner, ApplicationContextAware {

    public ApplicationContext applicationContext;

    @Override
    public List<JobExecutorInfo> doScan() {
        return scanJobExecutor();
    }

    private List<JobExecutorInfo> scanJobExecutor() {

        List<JobExecutorInfo> jobExecutorInfoList = new ArrayList<>();
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanDefinitionName);

            Map<Method, JobExecutor> annotatedMethods = null;
            try {
                annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(),
                    (MethodIntrospector.MetadataLookup<JobExecutor>) method -> AnnotatedElementUtils
                        .findMergedAnnotation(method, JobExecutor.class));
            } catch (Throwable ex) {
                SnailJobLog.LOCAL.error("{} JobExecutor加载异常：{}", beanDefinitionName, ex);
            }

            Class executorNotProxy = AopProxyUtils.ultimateTargetClass(bean);
            String executorClassName = executorNotProxy.getName();

            // 通过实现接口进行注册
            if (IJobExecutor.class.isAssignableFrom(bean.getClass())) {
                if (!JobExecutorInfoCache.isExisted(executorClassName)) {
                    jobExecutorInfoList.add(new JobExecutorInfo(executorClassName,
                        ReflectionUtils.findMethod(bean.getClass(), "jobExecute"),
                        null,null, null, bean));
                }
            }

            // 扫描类的注解
            JobExecutor jobExecutor = bean.getClass().getAnnotation(JobExecutor.class);
            if (Objects.nonNull(jobExecutor)) {
                String executorName = jobExecutor.name();
                if (!JobExecutorInfoCache.isExisted(executorName)) {
                    Method method = ReflectionUtils.findMethod(bean.getClass(), jobExecutor.method(), JobArgs.class);
                    if (method == null) {
                        method = ReflectionUtils.findMethod(bean.getClass(), jobExecutor.method());
                    }

                    // 扫描MapExecutor、ReduceExecutor、MergeReduceExecutor注解
                    Map<String, Method> mapExecutorMethodMap = new HashMap<>();
                    Method reduceExecutor = null;
                    Method mergeReduceExecutor = null;
                    Method[] methods = bean.getClass().getMethods();
                    for (final Method method1 : methods) {
                        Class<?>[] parameterTypes = method1.getParameterTypes();
                        MapExecutor mapExecutor = method1.getAnnotation(MapExecutor.class);
                        if (Objects.nonNull(mapExecutor)
                            && parameterTypes.length >0
                            && parameterTypes[0].isAssignableFrom(MapArgs.class)) {
                            mapExecutorMethodMap.put(mapExecutor.taskName(), method1);
                        }

                        ReduceExecutor reduceExecutorAnno = method1.getAnnotation(ReduceExecutor.class);
                        if (Objects.nonNull(reduceExecutorAnno)
                            && parameterTypes.length >0
                            && parameterTypes[0].isAssignableFrom(ReduceArgs.class)) {
                            reduceExecutor = method1;
                            continue;
                        }

                        MergeReduceExecutor mergeReduceExecutorAnno = method1.getAnnotation(MergeReduceExecutor.class);
                        if (Objects.nonNull(mergeReduceExecutorAnno)
                            && parameterTypes.length >0
                            && parameterTypes[0].isAssignableFrom(MergeReduceArgs.class)) {
                            mergeReduceExecutor = method1;
                        }
                    }

                    JobExecutorInfo jobExecutorInfo =
                        new JobExecutorInfo(
                            executorName,
                            method,
                            mapExecutorMethodMap,
                            reduceExecutor,
                            mergeReduceExecutor,
                            bean
                        );
                    jobExecutorInfoList.add(jobExecutorInfo);
                }

            }

            if (CollUtil.isEmpty(annotatedMethods)) {
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
                        null,null, null,
                        bean
                    );
                jobExecutorInfoList.add(jobExecutorInfo);
            }
        }

        return jobExecutorInfoList;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
