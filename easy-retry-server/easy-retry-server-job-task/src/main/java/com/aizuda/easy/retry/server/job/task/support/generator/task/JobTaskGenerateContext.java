package com.aizuda.easy.retry.server.job.task.support.generator.task;

import lombok.Data;

/**
 * @author opensnail
 * @date 2023-10-02 13:02:57
 * @since 2.4.0
 */
@Data
public class JobTaskGenerateContext {
    /**
     * 命名空间id
     */
    private String namespaceId;

    private Long taskBatchId;
    private String groupName;
    private Long jobId;
    private Integer routeKey;
    /**
     * 执行方法参数
     */
    private String argsStr;

    /**
     * 参数类型 text/json
     */
    private Integer argsType;
}
