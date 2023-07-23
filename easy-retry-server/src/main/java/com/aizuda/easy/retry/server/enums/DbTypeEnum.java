package com.aizuda.easy.retry.server.enums;

import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.baomidou.mybatisplus.annotation.DbType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 锁的存储介质
 *
 * @author www.byteblogs.com
 * @date 2023-06-04
 * @since 2.0
 */
@AllArgsConstructor
@Getter
public enum DbTypeEnum {
    MYSQL("mysql", "MySql数据库", DbType.MYSQL),
    MARIADB("mariadb", "MariaDB数据库", DbType.MARIADB),
    POSTGRE_SQL("postgresql", "Postgre数据库", DbType.POSTGRE_SQL);

    private final String db;
    private final String desc;
    private final DbType mpDbType;

    public static DbTypeEnum modeOf(String db) {
        for (DbTypeEnum value : DbTypeEnum.values()) {
            if (value.getDb() == db) {
                return value;
            }
        }

        throw new EasyRetryServerException("暂不支持此数据库 [{}]", db);
    }
}
