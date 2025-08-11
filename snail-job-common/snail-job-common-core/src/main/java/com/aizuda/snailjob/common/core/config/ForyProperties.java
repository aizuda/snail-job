package com.aizuda.snailjob.common.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author srzou
 * @date 2025/8/11 22:23
 */
@Data
@Configuration
@ConfigurationProperties(value = "snail-job.fory")
public class ForyProperties {
    /**
     * 解压大小
     * 单位：字节
     */
    private int decompressedSize = 16384;
}
