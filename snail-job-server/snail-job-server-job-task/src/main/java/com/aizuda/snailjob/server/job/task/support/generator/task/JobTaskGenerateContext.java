package com.aizuda.snailjob.server.job.task.support.generator.task;

import com.aizuda.snailjob.common.core.enums.MapReduceStageEnum;
import lombok.Data;

import java.util.List;

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

    /**
     * 动态分片的Map任务
     */
    private List<?> mapSubTask;

    /**
     * 任务名称
     */
    private String mapName;

    /**
     * 动态分片的阶段
     */
    private MapReduceStageEnum mrStage;

    /**
     * 父任务id
     */
    private Long parentId;
}
