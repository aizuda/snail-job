package com.aizuda.easy.retry.template.datasource.access.task;

import com.aizuda.easy.retry.template.datasource.access.TaskAccess;
import com.aizuda.easy.retry.template.datasource.enums.DbTypeEnum;
import com.aizuda.easy.retry.template.datasource.utils.DbUtils;
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

    protected static final List<String> ALLOW_DB = Arrays.asList(
        DbTypeEnum.MYSQL.getDb(),
        DbTypeEnum.MARIADB.getDb(),
        DbTypeEnum.POSTGRES.getDb(),
        DbTypeEnum.ORACLE.getDb(),
        DbTypeEnum.SQLSERVER.getDb());

    protected DbTypeEnum getDbType() {
        return DbUtils.getDbType();
    }

    /**
     * 设置分区
     *
     * @param groupName   组名称
     * @param namespaceId 命名空间id
     */
    public void setPartition(String groupName, String namespaceId) {
        RequestDataHelper.setPartition(groupName, namespaceId);
    }

    @Override
    public List<T> list(String groupName, String namespaceId, LambdaQueryWrapper<T> query) {
        setPartition(groupName, namespaceId);
        return doList(query);
    }

    @Override
    public int update(String groupName, String namespaceId, T t, LambdaUpdateWrapper<T> query) {
        setPartition(groupName, namespaceId);
        return doUpdate(t, query);
    }

    protected abstract int doUpdate(T t, LambdaUpdateWrapper<T> query);

    @Override
    public int updateById(String groupName, String namespaceId, T t) {
        setPartition(groupName, namespaceId);
        return doUpdateById(t);
    }

    @Override
    public int delete(String groupName, String namespaceId, LambdaQueryWrapper<T> query) {
        setPartition(groupName, namespaceId);
        return doDelete(query);
    }

    @Override
    public int insert(String groupName, String namespaceId, T t) {
        setPartition(groupName, namespaceId);
        return doInsert(t);
    }

    @Override
    public int batchInsert(String groupName, String namespaceId, List<T> list) {
        setPartition(groupName, namespaceId);
        return doBatchInsert(list);
    }

    protected abstract int doBatchInsert(List<T> list);

    @Override
    public PageDTO<T> listPage(String groupName, String namespaceId, final PageDTO<T> iPage, final LambdaQueryWrapper<T> query) {
        setPartition(groupName, namespaceId);
        return doListPage(iPage, query);
    }

    @Override
    public T one(String groupName, String namespaceId, LambdaQueryWrapper<T> query) {
        setPartition(groupName, namespaceId);
        return doOne(query);
    }

    protected abstract T doOne(LambdaQueryWrapper<T> query);

    protected abstract PageDTO<T> doListPage(final PageDTO<T> iPage, final LambdaQueryWrapper<T> query);

    @Override
    public long count(String groupName, String namespaceId, final LambdaQueryWrapper<T> query) {
        setPartition(groupName, namespaceId);
        return doCount(query);
    }

    protected abstract long doCount(final LambdaQueryWrapper<T> query);

    protected abstract int doInsert(T t);

    protected abstract int doDelete(LambdaQueryWrapper<T> query);

    protected abstract int doUpdateById(T t);

    protected abstract List<T> doList(LambdaQueryWrapper<T> query);
}
