package com.aizuda.snail.job.client.job.core.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: opensnail
 * @date : 2023-10-18 16:53
 * @since : 2.4.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ShardingJobArgs extends JobArgs {

    private Integer shardingTotal;

    private Integer shardingIndex;


}
