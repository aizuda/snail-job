package com.aizuda.snail.job.server.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aizuda.snail.job.common.core.util.JsonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @Author: opensnail
 * @Date: 2018/09/27 12:52
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper jacksonObjectMapper() {
        return JsonUtil.JsonMapper.jacksonObjectMapper();
    }

}
