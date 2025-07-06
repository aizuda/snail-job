package com.aizuda.snailjob.server.common.config;

import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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
