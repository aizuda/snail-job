package com.aizuda.snailjob.template.datasource.persistence.mapper;

import com.aizuda.snailjob.template.datasource.persistence.dataobject.DashboardCardResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.DashboardLineResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobSummary;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.DashboardRetryLineResponseDO.Rank;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.DashboardRetryLineResponseDO.Task;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.DashboardLineResponseDO;
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
public interface JobSummaryMapper extends BaseMapper<JobSummary> {

    int batchInsert(@Param("list") List<JobSummary> list);

    int batchUpdate(@Param("list") List<JobSummary> list);


    IPage<Task> jobTaskList(@Param("ew") Wrapper<Job> wrapper, Page<Object> page);

    List<DashboardLineResponseDO> jobLineList(@Param("dateFormat") String dateFormat, @Param("ew") Wrapper<JobSummary> wrapper);

    List<Rank> dashboardRank( @Param("systemTaskType") Integer systemTaskType, @Param("ew") Wrapper<JobSummary> wrapper);

    DashboardCardResponseDO.JobTask toJobTask(@Param("ew") Wrapper<JobSummary> wrapper);

    long countJobTask(@Param("ew") Wrapper<Job> wrapper);
}
