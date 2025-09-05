package com.aizuda.snailjob.client.common;

import com.aizuda.snailjob.client.common.annotation.Header;
import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.model.request.ConfigRequest;
import com.aizuda.snailjob.model.request.JobExecutorRequest;
import com.aizuda.snailjob.model.request.LogTaskRequest;
import com.aizuda.snailjob.model.request.RetryTaskRequest;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.enums.HeadersEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.model.Result;

import java.util.List;
import java.util.Map;


/**
 * rpc 客户端请求类
 *
 * @author: opensnail
 * @date : 2023-05-11 21:28
 * @since 1.3.0
 */
public interface RpcClient {

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.BATCH_REPORT)
    SnailJobRpcResult reportRetryInfo(List<RetryTaskRequest> list);

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.BATCH_LOG_REPORT)
    SnailJobRpcResult reportLogTask(List<LogTaskRequest> list);

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.SYNC_CONFIG)
    Result<ConfigRequest> syncRemoteConfig();

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.BEAT)
    Result<String> beat(String mark, @Header(name = HeadersEnum.LABEL) Map<String, String> labels);

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.REGISTER_JOB_EXECUTORS)
    Result registryExecutors(List<JobExecutorRequest> contextList);

}
