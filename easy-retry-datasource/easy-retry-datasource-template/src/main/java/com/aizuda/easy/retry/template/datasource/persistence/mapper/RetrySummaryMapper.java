package com.aizuda.easy.retry.template.datasource.persistence.mapper;

import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardCardResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardRetryLineResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardLineResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetrySummary;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhengweilin
 * @version 1.0.0
 * @date 2023/11/22
 */
@Mapper
public interface RetrySummaryMapper extends BaseMapper<RetrySummary> {

    int insertBatchRetrySummary(@Param("list") List<RetrySummary> list);

    int updateBatchSceneNameById(@Param("list") List<RetrySummary> list);

    DashboardCardResponseDO.RetryTask retryTask(@Param("namespaceId") String namespaceId);

    List<DashboardCardResponseDO.RetryTaskBar> retryTaskBarList(@Param("namespaceId") String namespaceId);

    IPage<DashboardRetryLineResponseDO.Task> retryTaskList(@Param("namespaceId") String namespaceId, Page<Object> page);

    List<DashboardLineResponseDO> retryLineList(@Param("namespaceId") String namespaceId,
                                                @Param("groupName") String groupName,
                                                @Param("type") String type,
                                                @Param("from") LocalDateTime from,
                                                @Param("to") LocalDateTime to);


    List<DashboardRetryLineResponseDO.Rank> dashboardRank(@Param("namespaceId") String namespaceId,
                                                          @Param("groupName") String groupName,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime
    );
}
