package com.x.retry.server.persistence.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.x.retry.server.persistence.mybatis.po.RetryTaskLog;

public interface RetryTaskLogMapper extends BaseMapper<RetryTaskLog> {

    int deleteByPrimaryKey(Long id);

    RetryTaskLog selectByPrimaryKey(Long id);

}