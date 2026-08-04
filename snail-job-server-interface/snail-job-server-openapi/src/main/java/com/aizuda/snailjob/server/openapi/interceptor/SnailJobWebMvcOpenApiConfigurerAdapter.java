package com.aizuda.snailjob.server.openapi.interceptor;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册 openapi 拦截器
 * @author opensnail
 * @date 2025-07-05
 */
@Configuration
public class SnailJobWebMvcOpenApiConfigurerAdapter implements WebMvcConfigurer {

    @Resource
    private OpenApiAuthenticationInterceptor openApiAuthenticationInterceptor;

    // 多个web拦截器配置，配置合并自动合并
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器,配置拦截的路径
        // OpenApi认证拦截器：基于客户端，如果使用openapi调用，则要求服务端配置的 token;
        // 如果使用openapi，务必确保 token 不要泄露，谨慎将服务端ui开放到外网
        registry.addInterceptor(openApiAuthenticationInterceptor).addPathPatterns("/api/**").order(2);
    }

}
