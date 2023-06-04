package com.aizuda.easy.retry.server.persistence.support;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 获取重试数据通道
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-19 11:39
 */
public interface RetryTaskAccess<T> {

    /**
     * 批量查询重试任务
     */
    List<T> listAvailableTasks(String groupName, LocalDateTime lastAt, Integer pageSize, Integer taskType);

    List<T> listRetryTaskByRetryCount(String groupName, Integer retryStatus);

    int deleteByDelayLevel(String groupName, Integer retryStatus);

    int updateRetryTask(T retryTask);

    int saveRetryTask(T retryTask);
}
