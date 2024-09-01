package com.aizuda.snailjob.client.job.core.executor;


import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import org.springframework.stereotype.Component;

@Component
@JobExecutor(name = "snailJobCMDJobExecutor")
public class SnailJobCMDJobExecutor extends CMDExecutor {

    public ExecuteResult jobExecute(JobArgs jobArgs) {
        String scriptParam = (String) jobArgs.getJobParams();
        return process(jobArgs.getTaskBatchId(), scriptParam);
    }

}
