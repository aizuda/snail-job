package com.x.retry.server.persistence.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.x.retry.server.persistence.mybatis.po.RetryTask;
import com.x.retry.server.web.model.request.RetryTaskQueryVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RetryTaskMapper extends BaseMapper<RetryTask> {

    int deleteBatch(@Param("ids") List<Long> ids, @Param("partition") Integer partition);

    int countAllRetryTask(@Param("partition") Integer partition);

}
