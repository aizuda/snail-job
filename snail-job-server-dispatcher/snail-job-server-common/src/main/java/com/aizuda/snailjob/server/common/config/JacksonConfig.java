package com.aizuda.snailjob.server.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.YYYY_MM_DD;
import static com.aizuda.snailjob.common.core.constant.SystemConstants.YYYY_MM_DD_HH_MM_SS;

/**
 * @Author: opensnail
 * @Date: 2018/09/27 12:52
 */
@Configuration
@Slf4j
public class JacksonConfig {

    @Bean
    public SimpleModule registerJavaTimeModule() {
        DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
        DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern(YYYY_MM_DD);
        // 全局配置序列化返回 JSON 处理
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(localDateFormatter));
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(localDateTimeFormatter));
        simpleModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(localDateFormatter));
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(localDateTimeFormatter));
        return simpleModule;
    }

    @Bean
    public JsonMapperBuilderCustomizer jsonInitCustomizer() {
        return builder -> {
            builder.defaultTimeZone(TimeZone.getDefault());
            log.info("初始化 jackson 配置");
        };
    }

}
