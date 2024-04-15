package com.aizuda.snail.job.template.datasource.access;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;

import java.util.List;

/**
 * 获取重试数据通道
 *
 * @author: opensnail
 * @date : 2021-11-19 11:39
 */
public interface TaskAccess<T> extends Access<T> {

    List<T> list(String groupName, String namespaceId, LambdaQueryWrapper<T> query);

    T one(String groupName, String namespaceId, LambdaQueryWrapper<T> query);

    int update(String groupName, String namespaceId, T t, LambdaUpdateWrapper<T> query);

    int updateById(String groupName, String namespaceId, T t);

    int delete(String groupName, String namespaceId, LambdaQueryWrapper<T> query);

    int insert(String groupName, String namespaceId, T t);

    int batchInsert(String groupName, String namespaceId, List<T> list);

    PageDTO<T> listPage(String groupName, String namespaceId, PageDTO<T> iPage, LambdaQueryWrapper<T> query);

    long count(String groupName, String namespaceId, LambdaQueryWrapper<T> query);

}
