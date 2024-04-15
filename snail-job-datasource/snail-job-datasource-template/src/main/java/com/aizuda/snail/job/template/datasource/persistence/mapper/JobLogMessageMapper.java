package com.aizuda.snail.job.template.datasource.persistence.mapper;

import com.aizuda.snail.job.template.datasource.persistence.po.JobLogMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 调度日志 Mapper 接口
 * </p>
 *
 * @author opensnail
 * @since 2023-09-24
 */
@Mapper
public interface JobLogMessageMapper extends BaseMapper<JobLogMessage> {

    int batchInsert(List<JobLogMessage> list);
}
