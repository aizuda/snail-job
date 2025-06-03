package com.aizuda.snailjob.server.common.rpc.server.handler;

import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.enums.HeadersEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.handler.GetHttpRequestHandler;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.po.JobExecutors;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 接收客户端执行器注册请求处理
 * @since 1.6.0
 */
@Component
@AllArgsConstructor
public class RegisterJobExecutorsHttpRequestHandler extends GetHttpRequestHandler {

//    private JobExecutorsMapper jobExecutorsMapper;
    private final AccessTemplate accessTemplate;
    @Override
    public boolean supports(String path) {
        return HTTP_PATH.REGISTER_JOB_EXECUTORS.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("Beat register job executors content:[{}]", content);
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object arg = retryRequest.getArgs()[0];
        if (arg == null) {
            return new SnailJobRpcResult();
        }
        String groupName = headers.get(HeadersEnum.GROUP_NAME.getKey());
        String namespace = headers.get(HeadersEnum.NAMESPACE.getKey());
        List<String> executors = (List<String>) arg;
        LambdaQueryWrapper<JobExecutors> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JobExecutors::getGroupName, groupName)
                .eq(JobExecutors::getNamespaceId, namespace);
        List<JobExecutors> dbExecutors = accessTemplate.getJobExecutorAccess().list(queryWrapper);
        List<String> dbExecutorsList = dbExecutors.stream().map(JobExecutors::getJobExecutorsName).toList();
        List<String> toAddExecutors = executors.stream().filter(e -> !dbExecutorsList.contains(e)).toList();
        if (toAddExecutors.isEmpty()) {
            return new SnailJobRpcResult();
        }
        List<JobExecutors> jobExecutorsList = toAddExecutors.stream().map(e -> {
            JobExecutors jobExecutors = new JobExecutors();
            jobExecutors.setGroupName(groupName);
            jobExecutors.setNamespaceId(namespace);
            jobExecutors.setJobExecutorsName(e);
            return jobExecutors;
        }).toList();
        accessTemplate.getJobExecutorAccess().insertBatch(jobExecutorsList);

        return new SnailJobRpcResult(StatusEnum.YES.getStatus(), "Batch Register Job Executors Processed Successfully", Boolean.TRUE, retryRequest.getReqId());
    }
}
