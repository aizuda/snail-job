package com.aizuda.snail.job.template.datasource.persistence.mapper;

import com.aizuda.snail.job.template.datasource.persistence.dataobject.DashboardCardResponseDO;
import com.aizuda.snail.job.template.datasource.persistence.dataobject.DashboardLineResponseDO;
import com.aizuda.snail.job.template.datasource.persistence.po.RetrySummary;
import com.aizuda.snail.job.template.datasource.persistence.po.SceneConfig;
import com.aizuda.snail.job.template.datasource.persistence.dataobject.DashboardRetryLineResponseDO.Rank;
import com.aizuda.snail.job.template.datasource.persistence.dataobject.DashboardRetryLineResponseDO.Task;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zhengweilin
 * @version 2.6.0
 * @date 2023/11/22
 */
@Mapper
public interface RetrySummaryMapper extends BaseMapper<RetrySummary> {

    int batchInsert(@Param("list") List<RetrySummary> list);

    int batchUpdate(@Param("list") List<RetrySummary> list);

    DashboardCardResponseDO.RetryTask retryTask(@Param("ew") Wrapper<RetrySummary> wrapper);

    List<DashboardCardResponseDO.RetryTask> retryTaskBarList(@Param("ew") Wrapper<RetrySummary> wrapper);

    IPage<Task> retryTaskList(@Param("ew") Wrapper<SceneConfig> wrapper, Page<Object> page);

    List<DashboardLineResponseDO> retryLineList(@Param("dateFormat") String dateFormat, @Param("ew") Wrapper<RetrySummary> wrapper);

    List<Rank> dashboardRank(@Param("ew") Wrapper<RetrySummary> wrapper);

    long countRetryTask(@Param("ew") Wrapper<SceneConfig> wrapper);
}
