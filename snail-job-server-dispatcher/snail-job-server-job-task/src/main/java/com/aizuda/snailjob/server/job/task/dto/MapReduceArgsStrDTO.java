package com.aizuda.snailjob.server.job.task.dto;

import lombok.Data;

/**
 * @author opensnail
 * @date 2024-06-25 22:58:05
 * @since sj_1.1.0
 */
@Data
public class MapReduceArgsStrDTO {

    private Integer shardNum;

    private String argsStr;
}
