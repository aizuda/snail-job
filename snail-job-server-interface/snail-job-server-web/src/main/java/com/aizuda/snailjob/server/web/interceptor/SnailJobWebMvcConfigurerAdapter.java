package com.aizuda.snailjob.server.web.interceptor;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author opensnail
 * @date 2022-03-06
 * @since 2.0
 */
@Configuration
public class SnailJobWebMvcConfigurerAdapter implements WebMvcConfigurer {

    @Autowired
    private LoginUserMethodArgumentResolver loginUserMethodArgumentResolver;
    @Autowired
    private CORSInterceptor corsInterceptor;
    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器,配置拦截的路径
        // 跨域CORS拦截器：追加跨域相关请求头
        registry.addInterceptor(corsInterceptor).addPathPatterns("/**").order(0);
        // web服务端认证拦截器：对于web服务端的直接调用拦截，按认证规则拦截；API调用交由 OpenApi认证拦截器 处理
        registry.addInterceptor(authenticationInterceptor).addPathPatterns("/**").excludePathPatterns("/api/**").order(1);

    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(loginUserMethodArgumentResolver);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/admin/**").addResourceLocations("classpath:/admin/");
    }

}
