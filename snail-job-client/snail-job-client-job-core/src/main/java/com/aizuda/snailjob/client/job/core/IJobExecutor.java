package com.aizuda.snailjob.client.job.core;


import com.aizuda.snailjob.common.core.model.JobContext;

/**
 * job执行者
 *
 * @author: opensnail
 * @date : 2023-09-27 09:38
 * @since 2.4.0
 */
public interface IJobExecutor {

    void jobExecute(JobContext jobContext);
}
