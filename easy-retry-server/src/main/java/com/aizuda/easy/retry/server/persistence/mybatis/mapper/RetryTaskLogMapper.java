package com.aizuda.easy.retry.server.persistence.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTaskLog;
import com.aizuda.easy.retry.server.web.model.response.DispatchQuantityResponseVO;
import com.aizuda.easy.retry.server.web.model.response.SceneQuantityRankResponseVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RetryTaskLogMapper extends BaseMapper<RetryTaskLog> {

    int deleteByPrimaryKey(Long id);

    RetryTaskLog selectByPrimaryKey(Long id);

    long countTaskTotal();

    long countTaskByRetryStatus(@Param("retryStatus") Integer retryStatus);

    List<SceneQuantityRankResponseVO> rankSceneQuantity(@Param("groupName") String groupName,
                                                        @Param("startTime")LocalDateTime startTime,
                                                        @Param("endTime")LocalDateTime endTime
                                                        );

    List<DispatchQuantityResponseVO> lineDispatchQuantity(@Param("groupName") String groupName,
                                                          @Param("retryStatus") Integer retryStatus,
                                                          @Param("type") String type,
                                                          @Param("startTime")LocalDateTime startTime,
                                                          @Param("endTime")LocalDateTime endTime
    );

}
