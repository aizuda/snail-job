package com.aizuda.snailjob.server.job.task.support.request;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.common.util.HttpHeaderUtil;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobSummaryMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobSummary;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;


/**
 * OPENAPI
 * 删除定时任务
 */
@Component
@RequiredArgsConstructor
public class OpenApiDeleteJobRequestHandler extends PostHttpRequestHandler {
    private final JobMapper jobMapper;
    private final JobSummaryMapper jobSummaryMapper;

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.OPENAPI_DELETE_JOB.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("Delete job content:[{}]", content);
        SnailJobRequest request = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = request.getArgs();
        Set<Long> ids = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), Set.class);
        String namespaceId = HttpHeaderUtil.getNamespace(headers);

        Assert.isTrue(ids.size() == jobMapper.delete(
                new LambdaQueryWrapper<Job>()
                        .eq(Job::getNamespaceId, namespaceId)
                        .eq(Job::getJobStatus, StatusEnum.NO.getStatus())
                        .in(Job::getId, ids)
        ), () -> new SnailJobServerException("Failed to delete scheduled task, please check if the task status is closed"));

        List<JobSummary> jobSummaries = jobSummaryMapper.selectList(new LambdaQueryWrapper<JobSummary>()
                .select(JobSummary::getId)
                .in(JobSummary::getBusinessId, ids)
                .eq(JobSummary::getNamespaceId, namespaceId)
                .eq(JobSummary::getSystemTaskType, SyetemTaskTypeEnum.JOB.getType())
        );
        if (CollUtil.isNotEmpty(jobSummaries)) {
            jobSummaryMapper.deleteByIds(StreamUtils.toSet(jobSummaries, JobSummary::getId));
        }

        return new SnailJobRpcResult(true, request.getReqId());
    }
}
