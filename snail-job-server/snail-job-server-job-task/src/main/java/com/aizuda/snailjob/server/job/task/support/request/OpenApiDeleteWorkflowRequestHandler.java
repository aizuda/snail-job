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
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobSummaryMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobSummary;
import com.aizuda.snailjob.template.datasource.persistence.po.Workflow;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;


/**
 * OPENAPI
 * 删除工作流
 */
@Component
@RequiredArgsConstructor
@Deprecated
public class OpenApiDeleteWorkflowRequestHandler extends PostHttpRequestHandler {
    private final WorkflowMapper workflowMapper;
    private final JobSummaryMapper jobSummaryMapper;

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.OPENAPI_DELETE_WORKFLOW.equals(path);
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

        Assert.isTrue(ids.size() == workflowMapper.delete(
                new LambdaQueryWrapper<Workflow>()
                        .eq(Workflow::getNamespaceId, namespaceId)
                        .eq(Workflow::getWorkflowStatus, StatusEnum.NO.getStatus())
                        .in(Workflow::getId, ids)
        ), () -> new SnailJobServerException("Failed to delete workflow task, please check if the task status is closed"));

        List<JobSummary> jobSummaries = jobSummaryMapper.selectList(new LambdaQueryWrapper<JobSummary>()
                .select(JobSummary::getId)
                .in(JobSummary::getBusinessId, ids)
                .eq(JobSummary::getNamespaceId, namespaceId)
                .eq(JobSummary::getSystemTaskType, SyetemTaskTypeEnum.WORKFLOW.getType())
        );
        if (CollUtil.isNotEmpty(jobSummaries)) {
            Assert.isTrue(jobSummaries.size() ==
                            jobSummaryMapper.deleteByIds(StreamUtils.toSet(jobSummaries, JobSummary::getId)),
                    () -> new SnailJobServerException("Summary table deletion failed")
            );
        }
        return new SnailJobRpcResult(true, request.getReqId());
    }
}
