package com.aizuda.easy.retry.client.job.core;


import com.aizuda.easy.retry.client.common.dto.JobContext;

/**
 * job执行者
 *
 * @author: www.byteblogs.com
 * @date : 2023-09-27 09:38
 * @since 2.4.0
 */
public interface IJobExecutor {
    void jobExecute(JobContext jobContext);
}
