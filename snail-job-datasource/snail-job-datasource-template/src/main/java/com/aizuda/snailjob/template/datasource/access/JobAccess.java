package com.aizuda.snailjob.template.datasource.access;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-03-29
 */
public interface JobAccess<T> extends Access<T> {

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
