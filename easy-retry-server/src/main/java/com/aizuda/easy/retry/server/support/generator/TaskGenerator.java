package com.aizuda.easy.retry.server.support.generator;

import com.aizuda.easy.retry.server.support.generator.task.TaskContext;

/**
 * 任务生成器
 *
 * @author www.byteblogs.com
 * @date 2023-07-16 11:42:38
 * @since 2.1.0
 */
public interface TaskGenerator {

    /**
     * 获取匹配的模式
     *
     * @param scene 1. 客户端上报 2.控制台新增单个任务 3.控制台批量新增任务
     * @return 符合条件的生成器
     */
    boolean supports(int scene);

    /**
     * 任务生成器
     *
     * @param taskContext 任务列表
     */
    void taskGenerator(TaskContext taskContext);
}
