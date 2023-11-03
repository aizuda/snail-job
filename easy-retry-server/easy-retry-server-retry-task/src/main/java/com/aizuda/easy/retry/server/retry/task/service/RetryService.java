package com.aizuda.easy.retry.server.retry.task.service;

import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;

import java.util.List;

/**
 * 重试服务层
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-26 15:17
 */
public interface RetryService {


    /**
     * 迁移到达最大重试次数到死信队列
     * 删除重试完成的数据
     *
     * @param groupId 组id
     * @return true- 处理成功 false- 处理失败
     */
    Boolean moveDeadLetterAndDelFinish(String groupId);
}
