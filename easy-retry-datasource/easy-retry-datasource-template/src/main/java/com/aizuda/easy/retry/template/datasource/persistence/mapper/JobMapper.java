package com.aizuda.easy.retry.template.datasource.persistence.mapper;

import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 任务信息 Mapper 接口
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2023-09-24
 */
@Mapper
public interface JobMapper extends BaseMapper<Job> {

}
