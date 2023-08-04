package com.aizuda.easy.retry.template.datasource.persistence.mapper;

import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RetryTaskMapper extends BaseMapper<RetryTask> {

    int deleteBatch(@Param("ids") List<Long> ids, @Param("partition") Integer partition);

    int countAllRetryTaskByRetryStatus(@Param("partition") Integer partition,
                                       @Param("retryStatus") Integer retryStatus);

    int batchInsert(@Param("list") List<RetryTask> list,  @Param("partition") Integer partition);

}
