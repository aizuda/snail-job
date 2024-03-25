package com.aizuda.easy.retry.sqlserver.datasource.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author: www.byteblogs.com
 * @date : 2024-03-19 22:05
 */
@Configuration
@ComponentScan("com.aizuda.easy.retry.sqlserver.datasource.*")
@ConditionalOnProperty(prefix = "easy-retry", name = "db-type", havingValue = "sqlserver")
public class EasyRetrySqlServerAutoConfiguration {

}
