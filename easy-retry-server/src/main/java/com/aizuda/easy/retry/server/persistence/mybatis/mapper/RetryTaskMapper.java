package com.aizuda.easy.retry.server.persistence.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RetryTaskMapper extends BaseMapper<RetryTask> {

    int deleteBatch(@Param("ids") List<Long> ids, @Param("partition") Integer partition);

    int countAllRetryTaskByRetryStatus(@Param("partition") Integer partition,
                                       @Param("retryStatus") Integer retryStatus);

    int batchInsert(@Param("list") List<RetryTask> list,  @Param("partition") Integer partition);

}
