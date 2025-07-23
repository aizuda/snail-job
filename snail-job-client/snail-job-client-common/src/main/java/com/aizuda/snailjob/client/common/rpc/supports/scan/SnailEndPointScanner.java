package com.aizuda.snailjob.client.common.rpc.supports.scan;

import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.annotation.SnailEndPoint;
import com.aizuda.snailjob.common.log.SnailJobLog;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author opensnail
 * @date 2024-04-11 22:29:07
 * @since 3.3.0
 */
@Component
public class SnailEndPointScanner implements ApplicationContextAware {

    private ApplicationContext context;

    public List<EndPointInfo> doScan() {
        return scanEndPoint();
    }

    private List<EndPointInfo> scanEndPoint() {

        List<EndPointInfo> endPointInfoList = new ArrayList<>();
        String[] beanDefinitionNames = context.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = context.getBean(beanDefinitionName);
            Class executorNotProxy = AopProxyUtils.ultimateTargetClass(bean);
            String executorClassName = executorNotProxy.getName();

            // 扫描类的注解
            SnailEndPoint jobExecutor = (SnailEndPoint) executorNotProxy.getAnnotation(SnailEndPoint.class);
            if (Objects.nonNull(jobExecutor)) {
                Map<Method, Mapping> annotatedMethods = null;
                try {
                    annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(),
                            (MethodIntrospector.MetadataLookup<Mapping>) method -> AnnotatedElementUtils
                                    .findMergedAnnotation(method, Mapping.class));
                } catch (Throwable ex) {
                    SnailJobLog.LOCAL.error("Mapping load exception for {}: {}", beanDefinitionName, ex);
                }

                for (Map.Entry<Method, Mapping> entry : annotatedMethods.entrySet()) {
                    Method method = entry.getKey();
                    Mapping mapping = entry.getValue();
                    endPointInfoList.add(EndPointInfo.builder()
                            .executorName(executorClassName)
                            .method(method)
                            .executor(bean)
                            .path(mapping.path())
                            .requestMethod(mapping.method())
                            .build());
                }
            }

        }

        return endPointInfoList;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }
}
