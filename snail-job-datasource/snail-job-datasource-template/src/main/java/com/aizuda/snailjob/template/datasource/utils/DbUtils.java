package com.aizuda.snailjob.template.datasource.utils;

import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.exception.SnailJobDatasourceException;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.regex.Pattern;

/**
 * 数据库工具
 *
 * @author: 疯狂的狮子Li
 * @date : 2024-03-27 14:17
 */
public class DbUtils {

    private static DbTypeEnum DEFAULT_DB_TYPE_ENUM = null;

    public static DbTypeEnum getDbType() {
        if (DEFAULT_DB_TYPE_ENUM == null) {
            DataSource dataSource = SnailSpringContext.getBean(DataSource.class);
            DEFAULT_DB_TYPE_ENUM = getDbType(dataSource);
        }
        return DEFAULT_DB_TYPE_ENUM;
    }

    /**
     * 获取当前配置的 DbType
     */
    public static DbTypeEnum getDbType(DataSource dataSource) {
        String jdbcUrl = getJdbcUrl(dataSource);
        if (StringUtils.hasText(jdbcUrl)) {
            return parseDbType(jdbcUrl);
        }

        throw new IllegalStateException("Can not get dataSource jdbcUrl: " + dataSource.getClass().getName());
    }

    /**
     * 通过数据源中获取 jdbc 的 url 配置
     * 符合 HikariCP, druid, c3p0, DBCP, beecp 数据源框架 以及 MyBatis UnpooledDataSource 的获取规则
     * UnpooledDataSource 参考 @{@link UnpooledDataSource#getUrl()}
     *
     * @return jdbc url 配置
     */
    public static String getJdbcUrl(DataSource dataSource) {
        String[] methodNames = new String[]{"getUrl", "getJdbcUrl"};
        for (String methodName : methodNames) {
            try {
                Method method = dataSource.getClass().getMethod(methodName);
                return (String) method.invoke(dataSource);
            } catch (Exception e) {
                //ignore
            }
        }

        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getURL();
        } catch (Exception e) {
            throw new SnailJobDatasourceException("Can not get the dataSource jdbcUrl.");
        }
    }


    /**
     * 参考 druid  和 MyBatis-plus 的 JdbcUtils
     * {@link com.alibaba.druid.util.JdbcUtils#getDbType(String, String)}
     * {@link com.baomidou.mybatisplus.extension.toolkit.JdbcUtils#getDbType(String)}
     *
     * @param jdbcUrl jdbcURL
     * @return 返回数据库类型
     */
    public static DbTypeEnum parseDbType(String jdbcUrl) {
        jdbcUrl = jdbcUrl.toLowerCase();
        if (jdbcUrl.contains(":ch:") || jdbcUrl.contains(":clickhouse:")) {
            return DbTypeEnum.CLICK_HOUSE;
        } else if (jdbcUrl.contains(":cobar:")) {
            return DbTypeEnum.MYSQL;
        } else if (jdbcUrl.contains(":csiidb:")) {
            return DbTypeEnum.CSIIDB;
        } else if (jdbcUrl.contains(":cubrid:")) {
            return DbTypeEnum.CUBRID;
        } else if (jdbcUrl.contains(":db2:")) {
            return DbTypeEnum.DB2;
        } else if (jdbcUrl.contains(":derby:")) {
            return DbTypeEnum.DERBY;
        } else if (isMatchedRegex(":dm\\d*:", jdbcUrl)) {
            return DbTypeEnum.DM;
        } else if (jdbcUrl.contains(":duckdb:")) {
            return DbTypeEnum.DUCKDB;
        } else if (jdbcUrl.contains(":firebirdsql:")) {
            return DbTypeEnum.FIREBIRD;
        } else if (jdbcUrl.contains(":gaussdb:") || jdbcUrl.contains(":zenith:")) {
            return DbTypeEnum.GAUSS;
        } else if (jdbcUrl.contains(":gbase:")) {
            return DbTypeEnum.GBASE;
        } else if (jdbcUrl.contains(":gbase8c:")) {
            return DbTypeEnum.GBASE_8C;
        } else if (jdbcUrl.contains(":gbase8s-pg:")) {
            return DbTypeEnum.GBASE_8S_PG;
        } else if (jdbcUrl.contains(":gbasedbt-sqli:") || jdbcUrl.contains(":informix-sqli:")) {
            return DbTypeEnum.GBASE_8S;
        } else if (jdbcUrl.contains(":goldendb:")) {
            return DbTypeEnum.GOLDENDB;
        } else if (jdbcUrl.contains(":goldilocks:")) {
            return DbTypeEnum.GOLDILOCKS;
        } else if (jdbcUrl.contains(":greenplum:")) {
            return DbTypeEnum.GREENPLUM;
        } else if (jdbcUrl.contains(":h2:")) {
            return DbTypeEnum.H2;
        } else if (jdbcUrl.contains(":highgo:")) {
            return DbTypeEnum.HIGH_GO;
        } else if (jdbcUrl.contains(":hive2:") || jdbcUrl.contains(":inceptor2:")) {
            return DbTypeEnum.HIVE;
        } else if (jdbcUrl.contains(":hsqldb:")) {
            return DbTypeEnum.HSQL;
        } else if (jdbcUrl.contains(":impala:")) {
            return DbTypeEnum.IMPALA;
        } else if (jdbcUrl.contains(":informix")) {
            return DbTypeEnum.INFORMIX;
        } else if (jdbcUrl.contains(":kingbase\\d*:") && isMatchedRegex(":kingbase\\d*:", jdbcUrl)) {
            return DbTypeEnum.KINGBASE_ES;
        } else if (jdbcUrl.contains(":lealone:")) {
            return DbTypeEnum.LEALONE;
        } else if (jdbcUrl.contains(":mariadb:")) {
            return DbTypeEnum.MARIADB;
        } else if (jdbcUrl.contains(":mysql:")) {
            return DbTypeEnum.MYSQL;
        } else if (jdbcUrl.contains(":oceanbase:")) {
            return DbTypeEnum.OCEAN_BASE;
        } else if (jdbcUrl.contains(":opengauss:")) {
            return DbTypeEnum.OPENGAUSS;
        } else if (jdbcUrl.contains(":oracle:")) {
            return DbTypeEnum.ORACLE;
        } else if (jdbcUrl.contains(":oscar:")) {
            return DbTypeEnum.OSCAR;
        } else if (jdbcUrl.contains(":phoenix:")) {
            return DbTypeEnum.PHOENIX;
        } else if (jdbcUrl.contains(":postgresql:")) {
            return DbTypeEnum.POSTGRE_SQL;
        } else if (jdbcUrl.contains(":presto:")) {
            return DbTypeEnum.PRESTO;
        } else if (jdbcUrl.contains(":redshift:")) {
            return DbTypeEnum.REDSHIFT;
        } else if (jdbcUrl.contains(":sap:")) {
            return DbTypeEnum.SAP_HANA;
        } else if (jdbcUrl.contains(":sinodb")) {
            return DbTypeEnum.SINODB;
        } else if (jdbcUrl.contains(":sqlite:")) {
            return DbTypeEnum.SQLITE;
        } else if (jdbcUrl.contains(":sqlserver:")) {
            return DbTypeEnum.SQLSERVER_2005;
        } else if (jdbcUrl.contains(":sqlserver2012:")) {
            return DbTypeEnum.SQLSERVER;
        } else if (jdbcUrl.contains(":sundb:")) {
            return DbTypeEnum.SUNDB;
        } else if (jdbcUrl.contains(":sybase:")) {
            return DbTypeEnum.SYBASE;
        } else if (jdbcUrl.contains(":taos:") || jdbcUrl.contains(":taos-rs:")) {
            return DbTypeEnum.TDENGINE;
        } else if (jdbcUrl.contains(":trino:")) {
            return DbTypeEnum.TRINO;
        } else if (jdbcUrl.contains(":uxdb:")) {
            return DbTypeEnum.UXDB;
        } else if (jdbcUrl.contains(":vastbase:")) {
            return DbTypeEnum.VASTBASE;
        } else if (jdbcUrl.contains(":vertica:")) {
            return DbTypeEnum.VERTICA;
        } else if (jdbcUrl.contains(":xcloud:")) {
            return DbTypeEnum.XCloud;
        } else if (jdbcUrl.contains(":xugu:")) {
            return DbTypeEnum.XUGU;
        } else if (jdbcUrl.contains(":yasdb:")) {
            return DbTypeEnum.YASDB;
        } else {
            return DbTypeEnum.OTHER;
        }
    }

    /**
     * 正则匹配，验证成功返回 true，验证失败返回 false
     */
    public static boolean isMatchedRegex(String regex, String jdbcUrl) {
        if (null == jdbcUrl) {
            return false;
        }
        return Pattern.compile(regex).matcher(jdbcUrl).find();
    }
}
