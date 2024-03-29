package com.aizuda.easy.retry.template.datasource.persistence.mapper;

import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardCardResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardRetryLineResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardLineResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetrySummary;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhengweilin
 * @version 2.6.0
 * @date 2023/11/22
 */
@Mapper
public interface RetrySummaryMapper extends BaseMapper<RetrySummary> {

    int insertOrUpdate(@Param("list") List<RetrySummary> list);

    DashboardCardResponseDO.RetryTask retryTask(@Param("ew") Wrapper<RetrySummary> wrapper);

    List<DashboardCardResponseDO.RetryTask> retryTaskBarList(@Param("ew") Wrapper<RetrySummary> wrapper);

    IPage<DashboardRetryLineResponseDO.Task> retryTaskList(@Param("ew") Wrapper<SceneConfig> wrapper, Page<Object> page);

    List<DashboardLineResponseDO> retryLineList(String type, @Param("ew") Wrapper<RetrySummary> wrapper);

    List<DashboardRetryLineResponseDO.Rank> dashboardRank(@Param("ew") Wrapper<RetrySummary> wrapper);
}
