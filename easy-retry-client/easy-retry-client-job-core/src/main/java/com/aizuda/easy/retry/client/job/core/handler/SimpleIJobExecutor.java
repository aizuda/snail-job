package com.aizuda.easy.retry.client.job.core.handler;

import com.aizuda.easy.retry.client.job.core.dto.ExecuteResult;
import com.aizuda.easy.retry.client.job.core.dto.JobContext;

/**
 * @author www.byteblogs.com
 * @date 2023-09-27 22:20:36
 * @since 2.4.0
 */
public abstract class SimpleIJobExecutor extends AbstractIJobExecutor {

    @Override
   public ExecuteResult jobExecute(JobContext jobContext) {
        return null;
    }
}
