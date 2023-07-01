package com.aizuda.easy.retry.server.persistence.mybatis.mapper;

import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTaskLogMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 重试日志异常信息记录表 Mapper 接口
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2023-06-16
 */
@Mapper
public interface RetryTaskLogMessageMapper extends BaseMapper<RetryTaskLogMessage> {

}
