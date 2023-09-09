package com.aizuda.easy.retry.client.core.intercepter;

import com.aizuda.easy.retry.client.core.annotation.Retryable;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.aop.support.annotation.AnnotationMethodMatcher;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author www.byteblogs.com
 * @date 2023-08-23
 */
@Configuration
@Slf4j
public class EasyRetryPointcutAdvisor extends AbstractPointcutAdvisor implements IntroductionAdvisor, BeanFactoryAware, InitializingBean {
    private Advice advice;
    private Pointcut pointcut;
    private BeanFactory beanFactory;
    @Autowired
    private EasyRetryInterceptor easyRetryInterceptor;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.pointcut = buildPointcut();
        this.advice = buildAdvice();
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public ClassFilter getClassFilter() {
        return pointcut.getClassFilter();
    }

    @Override
    public Class<?>[] getInterfaces() {
        return new Class[] { Retryable.class };
    }

    @Override
    public void validateInterfaces() throws IllegalArgumentException {
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    protected Advice buildAdvice() {
        return easyRetryInterceptor;
    }

    protected Pointcut buildPointcut() {
        return new EasyRetryAnnotationMethodPointcut(Retryable.class);
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    private static final class EasyRetryAnnotationMethodPointcut extends StaticMethodMatcherPointcut {

        private final MethodMatcher methodResolver;

        EasyRetryAnnotationMethodPointcut(Class<? extends Annotation> annotationType) {
            this.methodResolver = new AnnotationMethodMatcher(annotationType, true);
            setClassFilter(new EasyRetryAnnotationClassOrMethodFilter(annotationType));
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return this.methodResolver.matches(method, targetClass);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof EasyRetryAnnotationMethodPointcut)) {
                return false;
            }
            EasyRetryAnnotationMethodPointcut otherAdvisor = (EasyRetryAnnotationMethodPointcut) other;
            return ObjectUtils.nullSafeEquals(this.methodResolver, otherAdvisor.methodResolver);
        }

    }

    private static final class EasyRetryAnnotationClassOrMethodFilter extends AnnotationClassFilter {

        private final AnnotationMethodsResolver methodResolver;

        EasyRetryAnnotationClassOrMethodFilter(Class<? extends Annotation> annotationType) {
            super(annotationType, true);
            this.methodResolver = new AnnotationMethodsResolver(annotationType);
        }

        @Override
        public boolean matches(Class<?> clazz) {
            return super.matches(clazz) || this.methodResolver.hasAnnotatedMethods(clazz);
        }

    }

    private static class AnnotationMethodsResolver {

        private final Class<? extends Annotation> annotationType;

        public AnnotationMethodsResolver(Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        public boolean hasAnnotatedMethods(Class<?> clazz) {
            final AtomicBoolean found = new AtomicBoolean(false);
            ReflectionUtils.doWithMethods(clazz, method -> {
                if (found.get()) {
                    return;
                }
                Annotation annotation = AnnotationUtils.findAnnotation(method, annotationType);
                if (annotation != null) {
                    found.set(true);
                }
            });
            return found.get();
        }

    }

}
