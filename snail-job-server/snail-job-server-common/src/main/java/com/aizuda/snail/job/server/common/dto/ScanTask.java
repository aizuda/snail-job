package com.aizuda.snail.job.server.common.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 扫描任务模型
 *
 * @author: ww.byteblogs.com
 * @date : 2023-06-05 16:30
 * @since 1.5.0
 */
@Data
public class ScanTask {

    private String namespaceId;

    private String groupName;

    private Set<Integer> buckets;

    private Integer groupPartition;
}
