package com.aizuda.easy.retry.template.datasource.persistence.mapper;

import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardCardResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardLineResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardRetryLineResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobNotifyConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobSummary;
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
public interface JobSummaryMapper extends BaseMapper<JobSummary> {

    int insertOrUpdate(@Param("list") List<JobSummary> list);

    IPage<DashboardRetryLineResponseDO.Task> jobTaskList(@Param("ew") Wrapper<Job> wrapper, Page<Object> page);

    List<DashboardLineResponseDO> jobLineList(@Param("ew") Wrapper<JobSummary> wrapper);

    List<DashboardRetryLineResponseDO.Rank> dashboardRank(@Param("ew") Wrapper<JobSummary> wrapper);

    DashboardCardResponseDO.JobTask toJobTask(@Param("ew") Wrapper<JobSummary> wrapper);
}
