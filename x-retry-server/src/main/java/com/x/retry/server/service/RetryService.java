package com.x.retry.server.service;

import com.x.retry.server.model.dto.RetryTaskDTO;

import java.util.List;

/**
 * 重试服务层
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-26 15:17
 */
public interface RetryService {

    /**
     * 单个上报接口
     *
     * @param retryTaskDTO {@link RetryTaskDTO} 重试上报DTO
     * @return true- 处理成功 false- 处理失败
     */
    Boolean reportRetry(RetryTaskDTO retryTaskDTO);

    /**
     * 批量上报
     *
     * @param retryTaskDTOList {@link RetryTaskDTO} 重试上报DTO 列表
     * @return true- 全部处理成功 false- 全部处理失败
     */
    Boolean batchReportRetry(List<RetryTaskDTO> retryTaskDTOList);

    /**
     * 迁移到达最大重试次数到死信队列
     * 删除重试完成的数据
     *
     * @param groupId 组id
     * @return true- 处理成功 false- 处理失败
     */
    Boolean moveDeadLetterAndDelFinish(String groupId);
}
