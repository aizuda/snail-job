package com.aizuda.easy.retry.mariadb.datasource.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author: www.byteblogs.com
 * @date : 2023-08-04 12:37
 */
@Configuration
@ComponentScan("com.aizuda.easy.retry.mariadb.datasource.*")
@ConditionalOnProperty(prefix = "easy-retry", name = "db-type", havingValue = "mariadb")
public class EasyRetryMariadbAutoConfiguration {

}
