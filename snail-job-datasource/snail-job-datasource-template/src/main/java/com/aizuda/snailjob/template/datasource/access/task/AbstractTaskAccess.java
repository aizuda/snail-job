package com.aizuda.snailjob.template.datasource.access.task;

import com.aizuda.snailjob.template.datasource.access.TaskAccess;
import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.utils.DbUtils;
import com.aizuda.snailjob.template.datasource.utils.RequestDataHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;

import java.util.Arrays;
import java.util.List;

/**
 * @author: opensnail
 * @date : 2021-11-24 14:27
 */
public abstract class AbstractTaskAccess<T> implements TaskAccess<T> {

    protected static final List<String> ALLOW_DB = Arrays.asList(
            DbTypeEnum.MYSQL.getDb(),
            DbTypeEnum.MARIADB.getDb(),
            DbTypeEnum.POSTGRES.getDb(),
            DbTypeEnum.ORACLE.getDb(),
            DbTypeEnum.SQLSERVER.getDb(),
            DbTypeEnum.DM.getDb(),
            DbTypeEnum.KINGBASE.getDb());

    protected DbTypeEnum getDbType() {
        return DbUtils.getDbType();
    }

    @Override
    public List<T> list(LambdaQueryWrapper<T> query) {
        return doList(query);
    }

    @Override
    public int update(T t, LambdaUpdateWrapper<T> query) {
        return doUpdate(t, query);
    }

    protected abstract int doUpdate(T t, LambdaUpdateWrapper<T> query);

    @Override
    public int updateById(T t) {
        return doUpdateById(t);
    }

    @Override
    public int delete(LambdaQueryWrapper<T> query) {
        return doDelete(query);
    }

    @Override
    public int insert(T t) {
        return doInsert(t);
    }

    @Override
    public int insertBatch(List<T> list) {
        return doInsertBatch(list);
    }

    protected abstract int doInsertBatch(List<T> list);

    @Override
    public PageDTO<T> listPage(final PageDTO<T> iPage, final LambdaQueryWrapper<T> query) {
        return doListPage(iPage, query);
    }

    @Override
    public T one(LambdaQueryWrapper<T> query) {
        return doOne(query);
    }

    protected abstract T doOne(LambdaQueryWrapper<T> query);

    protected abstract PageDTO<T> doListPage(final PageDTO<T> iPage, final LambdaQueryWrapper<T> query);

    @Override
    public long count(final LambdaQueryWrapper<T> query) {
        return doCount(query);
    }

    protected abstract long doCount(final LambdaQueryWrapper<T> query);

    protected abstract int doInsert(T t);

    protected abstract int doDelete(LambdaQueryWrapper<T> query);

    protected abstract int doUpdateById(T t);

    protected abstract List<T> doList(LambdaQueryWrapper<T> query);
}
