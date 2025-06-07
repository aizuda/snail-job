package com.aizuda.snailjob.template.datasource.config;

import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.handler.InjectionMetaObjectHandler;
import com.aizuda.snailjob.template.datasource.handler.SnailJobMybatisConfiguration;
import com.aizuda.snailjob.template.datasource.utils.DbUtils;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
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
import java.util.List;

/**
 * @author: opensnail
 * @date : 2023-08-04 12:37
 */
@Configuration
@ComponentScan("com.aizuda.snailjob.template.datasource.**")
@MapperScan(value = "com.aizuda.snailjob.template.datasource.persistence.mapper", sqlSessionTemplateRef = "sqlSessionTemplate")
public class SnailJobTemplateAutoConfiguration {

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

        String dbCompatibilityType = null;
        if (dbTypeEnum.equals(DbTypeEnum.OPENGAUSS)) {
            dbCompatibilityType = "gauss";
        } else if (dbTypeEnum.mysqlSameType()) {
            dbCompatibilityType = "mysql";
        } else if (dbTypeEnum.oracleSameType()) {
            dbCompatibilityType = "oracle";
        } else if (dbTypeEnum.postgresqlSameType()) {
            dbCompatibilityType = "postgresql";
        }

        // 先尝试寻找枚举名称匹配的mapper定义，如果未找到尝试使用兼容的数据库mapper兜底
        Resource[] specificMapperResource = resolver.getResources(MessageFormat.format("classpath*:/{0}/mapper/*.xml", dbTypeEnum.getName()));
        if (specificMapperResource.length == 0 && dbCompatibilityType != null) {
            specificMapperResource = resolver.getResources(MessageFormat.format("classpath*:/{0}/mapper/*.xml", dbCompatibilityType));
        }

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
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }
}
