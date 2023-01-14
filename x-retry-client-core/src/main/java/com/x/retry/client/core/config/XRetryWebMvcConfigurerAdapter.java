package com.x.retry.client.core.config;

import com.x.retry.client.core.intercepter.HeadersInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author www.byteblogs.com
 * @date 2022-03-06
 * @since 2.0
 */
@Configuration
public class XRetryWebMvcConfigurerAdapter implements WebMvcConfigurer {

    @Autowired
    private HeadersInterceptor headersInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器
        registry.addInterceptor(headersInterceptor).addPathPatterns("/retry/**");

    }

}
