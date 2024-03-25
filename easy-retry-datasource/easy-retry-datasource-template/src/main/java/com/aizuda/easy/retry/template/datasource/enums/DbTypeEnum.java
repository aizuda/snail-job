package com.aizuda.easy.retry.template.datasource.enums;

import com.aizuda.easy.retry.template.datasource.exception.EasyRetryDatasourceException;
import com.baomidou.mybatisplus.annotation.DbType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * DB数据库类型
 *
 * @author www.byteblogs.com
 * @date 2023-06-04
 * @since 2.1.0
 */
@AllArgsConstructor
@Getter
public enum DbTypeEnum {
    MYSQL("mysql", "MySql数据库", DbType.MYSQL),
    MARIADB("mariadb", "MariaDB数据库", DbType.MARIADB),
    POSTGRES("postgres", "Postgres数据库", DbType.POSTGRE_SQL),
    ORACLE("oracle", "Oracle数据库", DbType.ORACLE_12C),
    SQLSERVER("sqlserver", "SQLServer数据库", DbType.SQL_SERVER);

    private final String db;
    private final String desc;
    private final DbType mpDbType;

    public static DbTypeEnum modeOf(String db) {
        for (DbTypeEnum value : DbTypeEnum.values()) {
            if (Objects.equals(value.getDb(), db)) {
                return value;
            }
        }

        throw new EasyRetryDatasourceException("暂不支持此数据库 [{}]", db);
    }
}
