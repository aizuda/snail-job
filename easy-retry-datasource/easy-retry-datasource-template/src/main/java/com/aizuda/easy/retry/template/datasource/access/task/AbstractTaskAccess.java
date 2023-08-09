package com.aizuda.easy.retry.template.datasource.access.task;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.template.datasource.access.TaskAccess;
import com.aizuda.easy.retry.template.datasource.enums.DbTypeEnum;
import com.aizuda.easy.retry.template.datasource.exception.EasyRetryDatasourceException;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.utils.RequestDataHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    public IPage<T> listPage(final String groupName, final IPage<T> iPage, final LambdaQueryWrapper<T> query) {
        setPartition(groupName);
        return doListPage(iPage, query);
    }

    protected abstract IPage<T> doListPage(final IPage<T> iPage, final LambdaQueryWrapper<T> query);

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
