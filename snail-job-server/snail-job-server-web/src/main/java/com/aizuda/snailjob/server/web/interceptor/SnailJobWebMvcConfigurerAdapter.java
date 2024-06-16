package com.aizuda.snailjob.server.web.interceptor;

import cn.hutool.core.date.LocalDateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.List;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.YYYY_MM_DD_HH_MM_SS;

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
        // 注册拦截器
        registry.addInterceptor(authenticationInterceptor).addPathPatterns("/**");
        // 配置拦截的路径
        registry.addInterceptor(corsInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(loginUserMethodArgumentResolver);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/admin/**").addResourceLocations("classpath:/admin/");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String source) {
                try {
                    // 尝试解析为时间戳
                    Long timestamp = Long.valueOf(source);
                    return LocalDateTimeUtil.of(timestamp);
                } catch (NumberFormatException ignore) {
                    return LocalDateTimeUtil.parse(source, YYYY_MM_DD_HH_MM_SS);
                }
            }
        });
    }

}
