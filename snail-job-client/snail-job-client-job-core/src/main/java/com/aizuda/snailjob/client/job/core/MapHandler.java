package com.aizuda.snailjob.client.job.core;

import com.aizuda.snailjob.client.model.ExecuteResult;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2024-06-26
 * @version : sj_1.1.0
 */
public interface MapHandler<T> {

    /**
     * 执行MAP操作
     *
     * @param taskList 需要分片的集合(建议不超过200个, 超过500禁止分片.)
     * @param nextTaskName 下一次需要处理MAP的节点名称 (不能是ROOT_MAP)
     * @return ExecuteResult
     */
    ExecuteResult doMap(List<T> taskList, String nextTaskName);
}
