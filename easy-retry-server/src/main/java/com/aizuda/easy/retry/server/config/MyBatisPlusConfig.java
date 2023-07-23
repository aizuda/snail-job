package com.aizuda.easy.retry.server.config;

import com.aizuda.easy.retry.server.enums.DbTypeEnum;
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
    public MybatisPlusInterceptor mybatisPlusInterceptor(SystemProperties systemProperties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor());
        DbTypeEnum dbTypeEnum = systemProperties.getDbType();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbTypeEnum.getMpDbType()));

        return interceptor;
    }

    public DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor() {
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler((sql, tableName) -> {
            if (!TABLES.contains(tableName)) {
                return tableName;
            }
            Integer partition = RequestDataHelper.getPartition();
            RequestDataHelper.remove();
            return tableName + "_"+ partition;
        });

        return dynamicTableNameInnerInterceptor;
    }
}
