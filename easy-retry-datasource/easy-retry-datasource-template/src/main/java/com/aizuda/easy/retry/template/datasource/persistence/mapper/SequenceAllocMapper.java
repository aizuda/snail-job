package com.aizuda.easy.retry.template.datasource.persistence.mapper;

import com.aizuda.easy.retry.template.datasource.persistence.po.SequenceAlloc;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 号段模式序号ID分配表 Mapper 接口
 * </p>
 *
 * @author www.byteblogs.com
 * @date 2023-05-05
 * @since 1.2.0
 */
@Mapper
public interface SequenceAllocMapper extends BaseMapper<SequenceAlloc> {

}
