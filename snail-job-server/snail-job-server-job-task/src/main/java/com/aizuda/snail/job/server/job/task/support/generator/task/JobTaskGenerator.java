package com.aizuda.snail.job.server.job.task.support.generator.task;

import com.aizuda.snail.job.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snail.job.template.datasource.persistence.po.JobTask;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-10-02 10:43:58
 * @since 2.4.0
 */
public interface JobTaskGenerator {

    JobTaskTypeEnum getTaskInstanceType();

    List<JobTask> generate(JobTaskGenerateContext context);

}
