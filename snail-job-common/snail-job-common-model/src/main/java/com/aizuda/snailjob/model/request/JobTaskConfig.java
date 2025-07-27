package com.aizuda.snailjob.model.request;

import lombok.Data;

/**
 * @author xiaowoniu
 * @date 2023-12-30 21:42:59
 * @since 2.6.0
 */
@Data
public class JobTaskConfig {

    /**
     * 任务ID
     */
    private Long jobId;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 标签
     * json格式，如：{"key1":"value1","key2":"value2"}
     */
    private String labels;
}
