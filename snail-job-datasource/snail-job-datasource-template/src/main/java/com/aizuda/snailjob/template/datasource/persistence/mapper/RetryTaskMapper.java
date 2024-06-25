package com.aizuda.snailjob.template.datasource.persistence.mapper;

import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RetryTaskMapper extends BaseMapper<RetryTask> {

    int insertBatch(@Param("list") List<RetryTask> list);

    int updateBatchNextTriggerAtById(@Param("partition") Integer partition, @Param("list") List<RetryTask> list);

}
