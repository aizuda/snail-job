package com.aizuda.snailjob.template.datasource.persistence.mapper;

import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLogMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 重试日志异常信息记录表 Mapper 接口
 * </p>
 *
 * @author opensnail
 * @since 2023-06-16
 */
@Mapper
public interface RetryTaskLogMessageMapper extends BaseMapper<RetryTaskLogMessage> {

    int insertBatch(List<RetryTaskLogMessage> list);

    int updateBatch(List<RetryTaskLogMessage> list);
}
