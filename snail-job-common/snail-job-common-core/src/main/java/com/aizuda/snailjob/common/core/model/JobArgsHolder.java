package com.aizuda.snailjob.common.core.model;

import lombok.Data;

/**
 * 定时任务 sj_job_task的args_str对应的
 * 参数模型
 *
 * @author: opensnail
 * @date : 2024-06-19
 * @since : sj_1.1.0
 */
@Data
public class JobArgsHolder {

    /**
     * sj_job表输入的参数
     */
    private Object jobParams;

    /**
     * 动态分片 map节点的结果
     */
    private Object maps;

    /**
     * 动态分片 reduce执行的结果
     */
    private Object reduces;

}
