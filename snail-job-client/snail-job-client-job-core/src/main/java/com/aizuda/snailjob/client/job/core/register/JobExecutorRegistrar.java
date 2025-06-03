package com.aizuda.snailjob.client.job.core.register;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.RpcClient;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.client.job.core.Scanner;
import com.aizuda.snailjob.client.job.core.cache.JobExecutorInfoCache;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.server.model.dto.JobExecutorDTO;
import com.aizuda.snailjob.client.job.core.dto.JobExecutorInfo;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.log.SnailJobLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author: opensnail
 * @date : 2022-02-10 09:12
 */
@Component
@RequiredArgsConstructor
public class JobExecutorRegistrar implements Lifecycle {
    private final List<Scanner> scanners;
    private final List<JobExecutorDTO> contextList = new ArrayList<>();
    public static RpcClient CLIENT;
    // 存储需要使用 endsWith 判断的后缀
    private static final Set<String> END_WITH_SET = new HashSet<>(Arrays.asList(
            "AnnotationJobExecutor",
            "AnnotationMapJobExecutor",
            "AnnotationMapReduceJobExecutor"
    ));

    // 存储需要使用 equals 判断的字符串
    private static final Set<String> EQUALS_SET = new HashSet<>(Arrays.asList(
            "snailJobCMDJobExecutor",
            "snailJobHttpExecutor",
            "snailJobPowerShellJobExecutor",
            "snailJobShellJobExecutor"
    ));

    public void registerRetryHandler(JobExecutorInfo jobExecutorInfo) {
        SnailJobProperties properties = SnailSpringContext.getBean(SnailJobProperties.class);
        String executorName = jobExecutorInfo.getExecutorName();
        if (JobExecutorInfoCache.isExisted(executorName)) {
            throw new SnailJobClientException("Duplicate executor names are not allowed: {}", executorName);
        }
        JobExecutorInfoCache.put(jobExecutorInfo);

        // 排除内部注解执行器
        for (String suffix : END_WITH_SET) {
            if (executorName.endsWith(suffix)) {
                return;
            }
        }

        if (EQUALS_SET.contains(executorName)) {
            return;
        }
        JobExecutorDTO jobExecutorDTO = new JobExecutorDTO();
        jobExecutorDTO.setExecutorType("1");
        jobExecutorDTO.setJobExecutorsName(executorName);
        jobExecutorDTO.setGroupName(properties.getGroup());
        jobExecutorDTO.setNamespaceId(properties.getNamespace());
        contextList.add(jobExecutorDTO);
    }

    public void registerRetryHandler(List<JobExecutorInfo> contextList) {
        for (JobExecutorInfo jobExecutorInfo : contextList) {
            registerRetryHandler(jobExecutorInfo);
        }
    }

    @Override
    public void start() {
        CLIENT = RequestBuilder.<RpcClient, SnailJobRpcResult>newBuilder()
                .client(RpcClient.class)
                .callback(
                        rpcResult -> {
                            if (StatusEnum.NO.getStatus().equals(rpcResult.getStatus())) {
                                SnailJobLog.LOCAL.error("Job executors register error requestId:[{}] message:[{}]", rpcResult.getReqId(), rpcResult.getMessage());
                            }
                        })
                .build();
        for (Scanner scanner : scanners) {
            this.registerRetryHandler(scanner.doScan());
        }
        // 推送当前执行器至服务器
        // 需要获取当前执行器的group及ns
        try {
            if (!contextList.isEmpty()) {
                CLIENT.registryExecutors(contextList);
            }
        }catch (Exception e){
            SnailJobLog.LOCAL.error("Job executors register error", e);
        }
    }

    @Override
    public void close() {
    }
}
