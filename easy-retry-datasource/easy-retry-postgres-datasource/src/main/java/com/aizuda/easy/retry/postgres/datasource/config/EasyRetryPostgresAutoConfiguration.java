package com.aizuda.easy.retry.postgres.datasource.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author: www.byteblogs.com
 * @date : 2023-08-04 12:37
 */
@Configuration
@ComponentScan("com.aizuda.easy.retry.postgres.datasource.*")
@ConditionalOnProperty(prefix = "easy-retry", name = "db-type", havingValue = "postgres")
public class EasyRetryPostgresAutoConfiguration {

}
