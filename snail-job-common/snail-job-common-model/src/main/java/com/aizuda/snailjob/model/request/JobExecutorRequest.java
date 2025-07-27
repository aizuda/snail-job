package com.aizuda.snailjob.model.request;

import lombok.Data;

/**
 * @author zhouxuangang
 * @date 2025/6/3 23:11
 */
@Data
public class JobExecutorRequest {

    /**
     * 定时任务执行器名称
     */
    private String executorInfo;
}
