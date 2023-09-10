package com.aizuda.easy.retry.template.datasource.access.task;

import com.aizuda.easy.retry.template.datasource.access.TaskAccess;
import com.aizuda.easy.retry.template.datasource.enums.DbTypeEnum;
import com.aizuda.easy.retry.template.datasource.utils.RequestDataHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-24 14:27
 */
public abstract class AbstractTaskAccess<T> implements TaskAccess<T> {

    @Autowired
    protected Environment environment;
    protected static final List<String> ALLOW_DB =  Arrays.asList(DbTypeEnum.MYSQL.getDb(),
            DbTypeEnum.MARIADB.getDb(),
            DbTypeEnum.POSTGRES.getDb());

    protected DbTypeEnum getDbType() {
        String dbType = environment.getProperty("easy-retry.db-type");
        return DbTypeEnum.modeOf(dbType);
    }

    /**
     * 设置分区
     *
     * @param groupName 组名称
     */
    public void setPartition(String groupName) {
        RequestDataHelper.setPartition(groupName);
    }

    @Override
    public List<T> list(String groupName, LambdaQueryWrapper<T> query) {
        setPartition(groupName);
        return doList(query);
    }

    @Override
    public int update(String groupName, T t, LambdaUpdateWrapper<T> query) {
        setPartition(groupName);
        return doUpdate(t, query);
    }

    protected abstract int doUpdate(T t, LambdaUpdateWrapper<T> query);

    @Override
    public int updateById(String groupName, T t) {
        setPartition(groupName);
        return doUpdateById(t);
    }

    @Override
    public int delete(String groupName, LambdaQueryWrapper<T> query) {
        setPartition(groupName);
        return doDelete(query);
    }

    @Override
    public int insert(String groupName, T t) {
        setPartition(groupName);
        return doInsert(t);
    }

    @Override
    public int batchInsert(String groupName, List<T> list) {
        setPartition(groupName);
        return doBatchInsert(list);
    }

    protected abstract int doBatchInsert(List<T> list);

    @Override
    public PageDTO<T> listPage(final String groupName, final PageDTO<T> iPage, final LambdaQueryWrapper<T> query) {
        setPartition(groupName);
        return doListPage(iPage, query);
    }

    @Override
    public T one(String groupName, LambdaQueryWrapper<T> query) {
        setPartition(groupName);
        return doOne(query);
    }

    protected abstract T doOne(LambdaQueryWrapper<T> query);

    protected abstract PageDTO<T> doListPage(final PageDTO<T> iPage, final LambdaQueryWrapper<T> query);

    @Override
    public long count(final String groupName, final LambdaQueryWrapper<T> query) {
        setPartition(groupName);
        return doCount(query);
    }

    protected abstract long doCount(final LambdaQueryWrapper<T> query);

    protected abstract int doInsert(T t);

    protected abstract int doDelete(LambdaQueryWrapper<T> query);

    protected abstract int doUpdateById(T t);

    protected abstract List<T> doList(LambdaQueryWrapper<T> query);
}
