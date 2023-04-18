package com.aizuda.easy.retry.server.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2021-11-13
 * @since 2.0
 */
@Configuration
@Slf4j
public class MyBatisPlusConfig {

    private final static List<String> TABLES = Arrays.asList("retry_task", "retry_dead_letter");

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return interceptor;
    }

    public DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor() {
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler((sql, tableName) -> {
            if (!TABLES.contains(tableName)) {
                return tableName;
            }
            Integer partition = RequestDataHelper.getRequestData("partition");
            RequestDataHelper.remove();
            return tableName + "_"+ partition;
        });

        return dynamicTableNameInnerInterceptor;
    }
}
