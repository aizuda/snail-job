package com.aizuda.easy.retry.template.datasource.config;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.template.datasource.enums.DbTypeEnum;
import com.aizuda.easy.retry.template.datasource.utils.RequestDataHelper;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author: www.byteblogs.com
 * @date : 2023-08-04 12:37
 */
@Configuration
@ComponentScan("com.aizuda.easy.retry.template.datasource.**")
@MapperScan(value = "com.aizuda.easy.retry.template.datasource.persistence.mapper", sqlSessionTemplateRef  = "sqlSessionTemplate")
public class EasyRetryTemplateAutoConfiguration {
    @Bean("sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, Environment environment, MybatisPlusInterceptor mybatisPlusInterceptor, MybatisPlusProperties mybatisPlusProperties) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        String dbType = environment.getProperty("easy-retry.db-type");
        DbTypeEnum dbTypeEnum = DbTypeEnum.modeOf(dbType);
        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(MessageFormat.format("classpath*:/{0}/mapper/*.xml", dbTypeEnum.getDb())));
        factoryBean.setPlugins(mybatisPlusInterceptor);
        factoryBean.setTypeAliasesPackage(mybatisPlusProperties.getTypeAliasesPackage());
        factoryBean.setGlobalConfig(mybatisPlusProperties.getGlobalConfig());

        return factoryBean.getObject();
    }

    @Bean("sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    private final static List<String> TABLES = Arrays.asList("retry_task", "retry_dead_letter");

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(Environment environment) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        String tablePrefix = Optional.ofNullable(environment.getProperty("mybatis-plus.global-config.db-config.table-prefix")).orElse(StrUtil.EMPTY);
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor(tablePrefix));
        String dbType = environment.getProperty("easy-retry.db-type");
        DbTypeEnum dbTypeEnum = DbTypeEnum.modeOf(dbType);
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbTypeEnum.getMpDbType()));

        return interceptor;
    }

    public DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor(String tablePrefix) {
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler((sql, tableName) -> {
            if (TABLES.contains(tableName)) {
                Integer partition = RequestDataHelper.getPartition();
                RequestDataHelper.remove();
                tableName = tableName + StrUtil.UNDERLINE + partition;
            }

            return tableName.startsWith(tablePrefix) ? tableName : tablePrefix + tableName;
        });

        return dynamicTableNameInnerInterceptor;
    }
}
