package com.aizuda.snailjob.template.datasource.persistence.mapper;


import com.aizuda.snailjob.template.datasource.persistence.dataobject.DashboardRetryResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.DashboardRetryResponseDO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RetryTaskLogMapper extends BaseMapper<RetryTaskLog> {

    int batchInsert(List<RetryTaskLog> list);

    List<DashboardRetryResponseDO> retrySummaryRetryTaskLogList(@Param("ew") Wrapper<RetryTaskLog> wrapper);
}
