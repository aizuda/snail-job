package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTaskLog;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author: shuguang.zhang
 * @date : 2023-05-05 16:15
 */
@Mapper
public interface RetryTaskLogConverter {

    RetryTaskLogConverter INSTANCE = Mappers.getMapper(RetryTaskLogConverter.class);

    RetryTaskLog toRetryTask(RetryTask retryTask);
}
