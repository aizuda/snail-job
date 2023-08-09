package com.aizuda.easy.retry.template.datasource.access;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 获取重试数据通道
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-19 11:39
 */
public interface TaskAccess<T> extends Access<T> {

    List<T> list(String groupName, LambdaQueryWrapper<T> query);

    T one(String groupName, LambdaQueryWrapper<T> query);

    int update(String groupName, T t, LambdaUpdateWrapper<T> query);

    int updateById(String groupName, T t);

    int delete(String groupName, LambdaQueryWrapper<T> query);

    int insert(String groupName, T t);

    int batchInsert(String groupName, List<T> list);

    PageDTO<T> listPage(String groupName, PageDTO<T> iPage, LambdaQueryWrapper<T> query);

    long count(String groupName, LambdaQueryWrapper<T> query);

}
