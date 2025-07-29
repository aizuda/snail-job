package com.aizuda.snailjob.client.job.core.executor.builtin;


import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.model.dto.ExecuteResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import org.springframework.stereotype.Component;

@Component
@JobExecutor(name = "snailJobShellJobExecutor")
public class SnailJobShellJobExecutor extends AbstractShellExecutor {

    public ExecuteResult jobExecute(JobArgs jobArgs) {
        Object jobParams = jobArgs.getJobParams();
        ScriptParams scriptParams = JsonUtil.parseObject((String) jobParams, ScriptParams.class);
        return process(jobArgs.getJobId(), scriptParams);
    }

}
