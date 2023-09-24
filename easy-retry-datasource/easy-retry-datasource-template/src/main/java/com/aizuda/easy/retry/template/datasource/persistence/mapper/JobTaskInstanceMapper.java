package com.aizuda.easy.retry.template.datasource.persistence.mapper;

import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskInstance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 任务实例 Mapper 接口
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2023-09-24
 */
@Mapper
public interface JobTaskInstanceMapper extends BaseMapper<JobTaskInstance> {

}
