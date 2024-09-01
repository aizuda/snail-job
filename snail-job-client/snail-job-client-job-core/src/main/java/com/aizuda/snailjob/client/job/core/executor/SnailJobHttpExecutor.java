package com.aizuda.snailjob.client.job.core.executor;


import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@JobExecutor(name = "snailJobHttpExecutor")
public class SnailJobHttpExecutor extends AbstractHttpExecutor {

    public ExecuteResult jobExecute(JobArgs jobArgs) {
        Object jobParams = jobArgs.getJobParams();
        HttpParams httpParams = JsonUtil.parseObject((String) jobParams, HttpParams.class);
        Map<String, String> hashMap = new HashMap<>(3);
        hashMap.put(SystemConstants.SNAIL_JOB_CLIENT_GROUP, snailJobProperties.getGroup());
        hashMap.put(SystemConstants.SNAIL_JOB_CLIENT_GROUP_TOKEN, snailJobProperties.getToken());
        hashMap.put(SystemConstants.SNAIL_JOB_CLIENT_NAMESPACE, snailJobProperties.getNamespace());
        Map<String, String> headers = (httpParams.getHeaders() == null || httpParams.getHeaders().isEmpty()) ? new HashMap<>() : httpParams.getHeaders();
        headers.putAll(hashMap);
        httpParams.setHeaders(headers);
        return process(httpParams);
    }

}
