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
     * 执行 MAP 操作时需注意：
     * 1. 建议待分片的集合不超过 200 个，严禁超过 500 个；
     * 2. taskList 参数仅传递必要信息：
     *    - 数据过多可能导致数据库字段超长；
     *    - 服务端与客户端频繁交互，过大数据会影响 RPC 性能。
     *
     * @param taskList 需要分片的集合
     * @param nextTaskName 下一次需要处理MAP的节点名称 (不能是ROOT_MAP)
     * @return ExecuteResult
     */
    ExecuteResult doMap(List<T> taskList, String nextTaskName);
}
