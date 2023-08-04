package com.aizuda.easy.retry.template.datasource.access.task;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.template.datasource.access.TaskAccess;
import com.aizuda.easy.retry.template.datasource.enums.DbTypeEnum;
import com.aizuda.easy.retry.template.datasource.exception.EasyRetryDatasourceException;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.utils.RequestDataHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
    protected final static List<String> ALLOW_DB =  Arrays.asList(DbTypeEnum.MYSQL.getDb(),
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

    private String getGroupName(LambdaQueryWrapper<T> query) {

        String sqlSegment = query.getSqlSegment();
        int indexOf = sqlSegment.indexOf("group_name");
        if (indexOf < 0) {
            throw new EasyRetryDatasourceException("groupName can not be null");
        }

        // 获取 ew.paramNameValuePairs.MPGENVAL2
        String substring = sqlSegment.substring(indexOf + 15, sqlSegment.indexOf("}", indexOf));

        // 获取MPGENVAL2
        String key = substring.replace(Constants.WRAPPER + Constants.WRAPPER_PARAM_MIDDLE, "");
        Map<String, Object> paramNameValuePairs = query.getParamNameValuePairs();
        return (String) paramNameValuePairs.get(key);
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


    protected abstract int doInsert(T t);

    protected abstract int doDelete(LambdaQueryWrapper<T> query);

    protected abstract int doUpdateById(T t);

    protected abstract List<T> doList(LambdaQueryWrapper<T> query);
}
