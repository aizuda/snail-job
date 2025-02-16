package com.aizuda.snailjob.template.datasource.access;

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

    List<T> list(LambdaQueryWrapper<T> query);

    T one(LambdaQueryWrapper<T> query);

    int update(T t, LambdaUpdateWrapper<T> query);

    int updateById(T t);

    int delete(LambdaQueryWrapper<T> query);

    int insert(T t);

    int insertBatch(List<T> list);

    PageDTO<T> listPage(PageDTO<T> iPage, LambdaQueryWrapper<T> query);

    long count(LambdaQueryWrapper<T> query);

}
