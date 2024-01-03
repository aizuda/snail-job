package com.aizuda.easy.retry.template.datasource.utils;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

/**
 * LambdaUpdateWrapper 的拓展 支持指定列的自增、自减
 *
 * @author lizhongyuan
 */

public class LambdaUpdateExpandWrapper<T> extends LambdaUpdateWrapper<T> {

    public LambdaUpdateExpandWrapper(Class<T> entityClass) {
        super(entityClass);
    }

    /**
     * 指定列自增
     *
     * @param columns 列引用
     * @param value   增长值
     */
    public LambdaUpdateExpandWrapper<T> incrField(SFunction<T, ?> columns, Object value) {
        String columnsToString = super.columnToString(columns);
        String format = String.format("%s =  %s + %s", columnsToString, columnsToString, formatSqlMaybeWithParam("{0}", value));
        setSql(format);
        return this;
    }

    /**
     * 指定列自减
     *
     * @param columns 列引用
     * @param value   减少值
     */
    public LambdaUpdateExpandWrapper<T> descField(SFunction<T, ?> columns, Object value) {
        String columnsToString = super.columnToString(columns);
        String format = String.format("%s =  %s - %s", columnsToString, columnsToString, formatSqlMaybeWithParam("{0}", value));
        setSql(format);
        return this;
    }
}