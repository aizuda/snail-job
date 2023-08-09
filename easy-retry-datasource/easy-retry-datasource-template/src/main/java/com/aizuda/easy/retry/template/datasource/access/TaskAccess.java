package com.aizuda.easy.retry.template.datasource.access;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 获取重试数据通道
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-19 11:39
 */
public interface TaskAccess<T> extends Access<T> {

    /**
     * 批量查询重试任务
     */
    List<T> listAvailableTasks(String groupName, LocalDateTime lastAt, final Long lastId, Integer pageSize, Integer taskType);

    List<T> list(String groupName, LambdaQueryWrapper<T> query);

    int update(String groupName, T t, LambdaUpdateWrapper<T> query);

    int updateById(String groupName, T t);

    int delete(String groupName, LambdaQueryWrapper<T> query);

    int insert(String groupName, T t);

    IPage<T> listPage(String groupName, IPage<T> iPage, LambdaQueryWrapper<T> query);

    long count(String groupName, LambdaQueryWrapper<T> query);

}
