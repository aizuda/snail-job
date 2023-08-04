package com.aizuda.easy.retry.template.datasource.persistence.mapper;


import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DispatchQuantityResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.SceneQuantityRankResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RetryTaskLogMapper extends BaseMapper<RetryTaskLog> {


    RetryTaskLog selectByPrimaryKey(Long id);

    long countTaskTotal();

    long countTaskByRetryStatus(@Param("retryStatus") Integer retryStatus);

    List<SceneQuantityRankResponseDO> rankSceneQuantity(@Param("groupName") String groupName,
                                                        @Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime")LocalDateTime endTime
                                                        );

    List<DispatchQuantityResponseDO> lineDispatchQuantity(@Param("groupName") String groupName,
                                                          @Param("retryStatus") Integer retryStatus,
                                                          @Param("type") String type,
                                                          @Param("startTime")LocalDateTime startTime,
                                                          @Param("endTime")LocalDateTime endTime
    );
//
    int batchInsert(List<RetryTaskLog> list);

}
