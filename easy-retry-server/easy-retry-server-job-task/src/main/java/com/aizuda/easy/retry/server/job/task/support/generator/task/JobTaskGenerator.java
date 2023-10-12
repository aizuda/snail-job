package com.aizuda.easy.retry.server.job.task.support.generator.task;

import com.aizuda.easy.retry.server.job.task.enums.TaskTypeEnum;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 10:43:58
 * @since 2.4.0
 */
public interface JobTaskGenerator {

    TaskTypeEnum getTaskInstanceType();

    List<JobTask> generate(JobTaskGenerateContext context);

}
