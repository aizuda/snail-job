package com.x.retry.server.persistence.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.x.retry.server.persistence.mybatis.po.RetryTaskLog;
import com.x.retry.server.web.model.response.ActivePodQuantityResponseVO;
import com.x.retry.server.web.model.response.DispatchQuantityResponseVO;
import com.x.retry.server.web.model.response.SceneQuantityRankResponseVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RetryTaskLogMapper extends BaseMapper<RetryTaskLog> {

    int deleteByPrimaryKey(Long id);

    RetryTaskLog selectByPrimaryKey(Long id);

    long countTaskTotal();

    long countTaskByRetryStatus(@Param("retryStatus") Integer retryStatus);

    List<SceneQuantityRankResponseVO> rankSceneQuantity(@Param("groupName") String groupName);

    List<DispatchQuantityResponseVO> lineDispatchQuantity(@Param("groupName") String groupName,
                                                          @Param("retryStatus") Integer retryStatus);

}
