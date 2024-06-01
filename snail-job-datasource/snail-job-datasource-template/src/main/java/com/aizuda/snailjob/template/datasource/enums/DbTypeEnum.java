package com.aizuda.snailjob.template.datasource.enums;

import com.aizuda.snailjob.template.datasource.exception.SnailJobDatasourceException;
import com.baomidou.mybatisplus.annotation.DbType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DB数据库类型
 *
 * @author opensnail
 * @date 2023-06-04
 * @since 2.1.0
 */
@AllArgsConstructor
@Getter
public enum DbTypeEnum {
    MYSQL("mysql", "MySql数据库", DbType.MYSQL),
    MARIADB("mariadb", "MariaDB数据库", DbType.MARIADB),
    POSTGRES("postgresql", "Postgres数据库", DbType.POSTGRE_SQL),
    ORACLE("oracle", "Oracle数据库", DbType.ORACLE_12C),
    SQLSERVER("sqlserver", "SQLServer数据库", DbType.SQL_SERVER),
    DM("dm", "达梦数据库", DbType.DM);

    private final String db;
    private final String desc;
    private final DbType mpDbType;

    public static DbTypeEnum modeOf(String db) {
        for (DbTypeEnum value : DbTypeEnum.values()) {
            if (db.contains(value.getDb())) {
                return value;
            }
        }

        throw new SnailJobDatasourceException("暂不支持此数据库 [{}]", db);
    }
}
