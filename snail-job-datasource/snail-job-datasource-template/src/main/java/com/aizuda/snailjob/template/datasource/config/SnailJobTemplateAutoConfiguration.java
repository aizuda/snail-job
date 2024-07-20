package com.aizuda.snailjob.template.datasource.config;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.handler.InjectionMetaObjectHandler;
import com.aizuda.snailjob.template.datasource.handler.SnailJobMybatisConfiguration;
import com.aizuda.snailjob.template.datasource.utils.DbUtils;
import com.aizuda.snailjob.template.datasource.utils.RequestDataHelper;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author: opensnail
 * @date : 2023-08-04 12:37
 */
@Configuration
@ComponentScan("com.aizuda.snailjob.template.datasource.**")
@MapperScan(value = "com.aizuda.snailjob.template.datasource.persistence.mapper", sqlSessionTemplateRef = "sqlSessionTemplate")
public class SnailJobTemplateAutoConfiguration {

    /**
     * 采用后缀分区的数据库表清单
     */
    private static final List<String> TABLES_WITH_PARTITION = Arrays.asList("sj_retry_task", "sj_retry_dead_letter");

    @Bean("sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, Environment environment,
        MybatisPlusInterceptor mybatisPlusInterceptor,
        MybatisPlusProperties mybatisPlusProperties,
        SnailJobMybatisConfiguration snailJobMybatisConfiguration) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        DbTypeEnum dbTypeEnum = DbUtils.getDbType();

        // 动态设置mapper资源: 通用 + 数据库专用
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] specificMapperResource = resolver.getResources(MessageFormat.format("classpath*:/{0}/mapper/*.xml", dbTypeEnum.getDb()));
        Resource[] templateMapperResource = resolver.getResources("classpath*:/template/mapper/*.xml");
        List<Resource> resources = new ArrayList<>();
        resources.addAll(List.of(specificMapperResource));
        resources.addAll(List.of(templateMapperResource));
        factoryBean.setMapperLocations(resources.toArray(new Resource[0]));

        // 分页插件
        factoryBean.setPlugins(mybatisPlusInterceptor);
        factoryBean.setTypeAliasesPackage(mybatisPlusProperties.getTypeAliasesPackage());

        // 自动填充
        GlobalConfig globalConfig = mybatisPlusProperties.getGlobalConfig();
        globalConfig.setMetaObjectHandler(new InjectionMetaObjectHandler());

        factoryBean.setGlobalConfig(mybatisPlusProperties.getGlobalConfig());
        factoryBean.setConfiguration(snailJobMybatisConfiguration);

        return factoryBean.getObject();
    }

    @Bean("sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    public SnailJobMybatisConfiguration snailJobMybatisConfiguration() {
        return new SnailJobMybatisConfiguration();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(Environment environment) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        String tablePrefix = Optional.ofNullable(environment.getProperty("mybatis-plus.global-config.db-config.table-prefix")).orElse(StrUtil.EMPTY);
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor(tablePrefix));
        DbTypeEnum dbTypeEnum = DbUtils.getDbType();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbTypeEnum.getMpDbType()));

        return interceptor;
    }

    public DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor(String tablePrefix) {
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler((sql, tableName) -> {
            if (TABLES_WITH_PARTITION.contains(tableName)) {
                Integer partition = RequestDataHelper.getPartition();
                RequestDataHelper.remove();
                tableName = tableName + StrUtil.UNDERLINE + partition;
            }

            return tableName.startsWith(tablePrefix) ? tableName : tablePrefix + tableName;
        });

        return dynamicTableNameInnerInterceptor;
    }

}
