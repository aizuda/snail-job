package com.aizuda.snailjob.template.datasource.persistence.mapper;

import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 任务实例 Mapper 接口
 * </p>
 *
 * @author opensnail
 * @since 2023-09-24
 */
@Mapper
public interface JobTaskMapper extends BaseMapper<JobTask> {

    int insertBatch(@Param("list") List<JobTask> list);
}
