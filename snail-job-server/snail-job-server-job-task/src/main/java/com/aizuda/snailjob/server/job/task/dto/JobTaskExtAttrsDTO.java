package com.aizuda.snailjob.server.job.task.dto;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.enums.MapReduceStageEnum;
import lombok.Data;

/**
 * @author: opensnail
 * @date : 2024-06-12
 * @since : sj_1.1.0
 */
@Data
public class JobTaskExtAttrsDTO {

    /**
     * 任务名称(目前只有map reduce使用)
     */
    private String mapName;

    /**
     * see: {@link JobTaskTypeEnum}
     */
    private Integer taskType;

    /**
     * 当前任务处于map reduce的哪个阶段
     * see: {@link MapReduceStageEnum}
     */
    private String mrStage;


    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }
}
