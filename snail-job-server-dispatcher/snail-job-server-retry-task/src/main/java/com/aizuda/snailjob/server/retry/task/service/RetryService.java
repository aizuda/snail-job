package com.aizuda.snailjob.server.retry.task.service;

/**
 * 重试服务层
 *
 * @author: opensnail
 * @date : 2021-11-26 15:17
 */
public interface RetryService {


    /**
     * 迁移到达最大重试次数到死信队列
     * 删除重试完成的数据
     *
     * @param groupName   组id
     * @param namespaceId 命名空间id
     * @return true- 处理成功 false- 处理失败
     */
    Boolean moveDeadLetterAndDelFinish(String groupName, String namespaceId);
}
