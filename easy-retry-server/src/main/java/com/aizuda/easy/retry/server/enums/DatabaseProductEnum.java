package com.aizuda.easy.retry.server.enums;

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
public enum DatabaseProductEnum {
    MYSQL("mysql", "MySql数据库"),
    MARIADB("mariadb", "MariaDB数据库"),
    POSTGRE_SQL("postgresql", "Postgre数据库"),
    OTHER("other", "其他数据库");

    private final String db;
    private final String desc;
}
